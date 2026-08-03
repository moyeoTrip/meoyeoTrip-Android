package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFlowCoordinatorTest {
    @Test
    fun newUserFollowsNicknameInfoSignupAndProfileFlow() = runBlocking {
        val gateway = FakeAuthGateway()
        val sessionStore = InMemoryAuthSessionStore()
        val coordinator = coordinator(gateway, sessionStore)

        coordinator.login(AuthProvider.KAKAO)

        assertEquals(AuthDestination.NICKNAME, coordinator.state.destination)
        assertEquals(3, coordinator.state.nickname.candidates.size)
        coordinator.selectNickname("따스한 사슴 3492")
        coordinator.signup(Gender.FEMALE, "1998-04-12")

        assertEquals(AuthDestination.PROFILE_IMAGE, coordinator.state.destination)
        assertEquals("selection-token", gateway.lastSignupInput?.nicknameSelectionToken)
        assertEquals("따스한 사슴 3492", gateway.lastSignupInput?.nickname)
        assertEquals(Gender.FEMALE, gateway.lastSignupInput?.gender)
        assertEquals("1998-04-12", gateway.lastSignupInput?.birthDate)
        assertEquals(SignupState.PROFILE_IMAGE_REQUIRED, sessionStore.current.signupState)
    }

    @Test
    fun profileRequiredLoginResumesDirectlyAtImageStep() = runBlocking {
        val restored = listOf(
            ProfileImageCandidate(7, "https://cdn.example/7.png", false),
            ProfileImageCandidate(8, "https://cdn.example/8.png", true)
        )
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = "existing-access",
                refreshToken = "existing-refresh",
                isNewUser = false,
                signupState = SignupState.PROFILE_IMAGE_REQUIRED,
                providerType = AuthProvider.APPLE
            ),
            profileImages = profileImages(restored, generationCount = 2, remainingCount = 1)
        )
        val coordinator = coordinator(gateway, InMemoryAuthSessionStore())

        coordinator.login(AuthProvider.APPLE)

        assertEquals(AuthDestination.PROFILE_IMAGE, coordinator.state.destination)
        assertEquals(1, gateway.profileListRequests)
        assertEquals(restored, coordinator.state.profileImages?.candidates)
        assertEquals(8L, coordinator.state.selectedProfileImageId)
        assertNull(gateway.lastSignupInput)
    }

    @Test
    fun serverSignupStateOverridesStaleLocalCompletionFromAnotherDevice() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.APPLE,
                ServiceSession("stale-access", "stale-refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = "server-access",
                refreshToken = "server-refresh",
                isNewUser = false,
                signupState = SignupState.PROFILE_IMAGE_REQUIRED,
                providerType = AuthProvider.APPLE
            )
        )

        val coordinator = coordinator(gateway, store)
        coordinator.login(AuthProvider.APPLE)

        assertEquals(AuthDestination.PROFILE_IMAGE, coordinator.state.destination)
        assertEquals(SignupState.PROFILE_IMAGE_REQUIRED, store.current.signupState)
        assertEquals("server-access", store.current.accessToken)
    }

    @Test
    fun generationCountersComeOnlyFromServerResponse() = runBlocking {
        val emittedStates = mutableListOf<AuthFlowState>()
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = "access",
                refreshToken = "refresh",
                isNewUser = false,
                signupState = SignupState.PROFILE_IMAGE_REQUIRED,
                providerType = AuthProvider.EMAIL
            ),
            generatedImages = ProfileImageCandidates(
                candidates = listOf(ProfileImageCandidate(41, "https://cdn.example/41.png", false)),
                generationCount = 2,
                remainingGenerationCount = 1,
                signupState = SignupState.PROFILE_IMAGE_REQUIRED
            )
        )
        val coordinator = coordinator(gateway, InMemoryAuthSessionStore()) { emittedStates += it }
        coordinator.loginWithEmail(emailRequest())

        coordinator.generateProfileImage()

        assertTrue(emittedStates.any { it.isGeneratingProfileImage })
        assertFalse(coordinator.state.isGeneratingProfileImage)
        assertEquals(2, coordinator.state.profileImages?.generationCount)
        assertEquals(1, coordinator.state.profileImages?.remainingGenerationCount)
        assertEquals(41L, coordinator.state.selectedProfileImageId)
    }

    @Test
    fun generatedCandidateAccumulatesWithoutReplacingExistingCandidates() = runBlocking {
        val existing = ProfileImageCandidate(11, "https://cdn.example/11.png", false)
        val added = ProfileImageCandidate(12, "https://cdn.example/12.png", false)
        val gateway = FakeAuthGateway(
            loginResult = profileRequiredEmailLogin(),
            profileImages = profileImages(listOf(existing), generationCount = 1, remainingCount = 2),
            generatedImages = profileImages(listOf(added), generationCount = 2, remainingCount = 1)
        )
        val coordinator = coordinator(gateway, InMemoryAuthSessionStore())
        coordinator.loginWithEmail(emailRequest())

        coordinator.generateProfileImage()

        assertEquals(listOf(existing, added), coordinator.state.profileImages?.candidates)
        assertEquals(1, gateway.profileGenerationRequests)
        assertEquals(12L, coordinator.state.selectedProfileImageId)
    }

    @Test
    fun generationStopsWhenServerReportsNoRemainingAttempts() = runBlocking {
        val candidates = (1L..3L).map { id ->
            ProfileImageCandidate(id, "https://cdn.example/$id.png", false)
        }
        val gateway = FakeAuthGateway(
            loginResult = profileRequiredEmailLogin(),
            profileImages = profileImages(candidates, generationCount = 3, remainingCount = 0)
        )
        val coordinator = coordinator(gateway, InMemoryAuthSessionStore())
        coordinator.loginWithEmail(emailRequest())

        coordinator.generateProfileImage()

        assertEquals(0, gateway.profileGenerationRequests)
        assertEquals(candidates, coordinator.state.profileImages?.candidates)
    }

    @Test
    fun completedProfileMarksSessionAuthenticated() = runBlocking {
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = "access",
                refreshToken = "refresh",
                isNewUser = false,
                signupState = SignupState.PROFILE_IMAGE_REQUIRED,
                providerType = AuthProvider.EMAIL
            ),
            profileImages = ProfileImageCandidates(
                candidates = listOf(ProfileImageCandidate(7, "https://cdn.example/7.png", false)),
                generationCount = 1,
                remainingGenerationCount = 2,
                signupState = SignupState.PROFILE_IMAGE_REQUIRED
            )
        )
        val sessionStore = InMemoryAuthSessionStore()
        val coordinator = coordinator(gateway, sessionStore)
        coordinator.loginWithEmail(emailRequest())
        coordinator.selectProfileImage(7)

        coordinator.completeProfileImage()

        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
        assertEquals(7L, gateway.selectedProfileImageId)
        assertTrue(sessionStore.current.isAuthenticated)
        assertFalse(coordinator.state.isLoading)
    }

    @Test
    fun googleLoginUsesFirebaseTokenAndServerSignupState() = runBlocking {
        val provider = RecordingIdentityTokenProvider()
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = null,
                refreshToken = null,
                isNewUser = true,
                signupState = SignupState.USER_INFO_REQUIRED,
                providerType = AuthProvider.GOOGLE
            )
        )
        val coordinator = AuthFlowCoordinator(provider, gateway, InMemoryAuthSessionStore())

        coordinator.login(AuthProvider.GOOGLE)

        assertEquals(AuthProvider.GOOGLE, provider.lastProvider)
        assertEquals(AuthDestination.NICKNAME, coordinator.state.destination)
        assertEquals(AuthProvider.GOOGLE, coordinator.state.identity?.provider)
    }

    @Test
    fun emailCreateAccountObtainsFirebaseTokenBeforeBackendLogin() = runBlocking {
        val provider = RecordingIdentityTokenProvider()
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = "access",
                refreshToken = "refresh",
                isNewUser = false,
                signupState = SignupState.SIGNUP_COMPLETE,
                providerType = AuthProvider.EMAIL
            )
        )
        val coordinator = AuthFlowCoordinator(provider, gateway, InMemoryAuthSessionStore())
        val request = emailRequest(EmailAuthAction.CREATE_ACCOUNT)

        coordinator.loginWithEmail(request)

        assertEquals(request, provider.lastEmailRequest)
        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
    }

    @Test
    fun passwordResetDelegatesToFirebaseAndShowsConfirmation() = runBlocking {
        val provider = RecordingIdentityTokenProvider()
        val coordinator = AuthFlowCoordinator(provider, FakeAuthGateway(), InMemoryAuthSessionStore())

        coordinator.sendPasswordReset("trip@example.com")

        assertEquals("trip@example.com", provider.lastResetEmail)
        assertEquals("비밀번호 재설정 메일을 보냈어요.", coordinator.state.noticeMessage)
    }

    private fun coordinator(
        gateway: AuthGateway,
        store: AuthSessionStore,
        onStateChange: (AuthFlowState) -> Unit = {}
    ) = AuthFlowCoordinator(
        identityTokenProvider = RecordingIdentityTokenProvider(),
        authGateway = gateway,
        sessionStore = store,
        onStateChange = onStateChange
    )

    private fun emailRequest(action: EmailAuthAction = EmailAuthAction.SIGN_IN) = EmailAuthRequest(
        email = "trip@example.com",
        password = "password123",
        action = action
    )

    private fun profileRequiredEmailLogin() = LoginResult(
        accessToken = "access",
        refreshToken = "refresh",
        isNewUser = false,
        signupState = SignupState.PROFILE_IMAGE_REQUIRED,
        providerType = AuthProvider.EMAIL
    )

    private fun profileImages(candidates: List<ProfileImageCandidate>, generationCount: Int, remainingCount: Int) =
        ProfileImageCandidates(
            candidates = candidates,
            generationCount = generationCount,
            remainingGenerationCount = remainingCount,
            signupState = SignupState.PROFILE_IMAGE_REQUIRED
        )
}

private class RecordingIdentityTokenProvider : IdentityTokenProvider {
    var lastProvider: AuthProvider? = null
    var lastEmailRequest: EmailAuthRequest? = null
    var lastResetEmail: String? = null

    override suspend fun acquire(provider: AuthProvider): IdentityToken {
        lastProvider = provider
        return IdentityToken(provider, "firebase-id-token", "fcm-token")
    }

    override suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken {
        lastEmailRequest = request
        return IdentityToken(AuthProvider.EMAIL, "firebase-email-id-token", "fcm-token")
    }

    override suspend fun sendPasswordReset(email: String) {
        lastResetEmail = email
    }
}

private class FakeAuthGateway(
    private val loginResult: LoginResult = LoginResult(
        accessToken = null,
        refreshToken = null,
        isNewUser = true,
        signupState = SignupState.USER_INFO_REQUIRED,
        providerType = AuthProvider.KAKAO
    ),
    private val profileImages: ProfileImageCandidates = ProfileImageCandidates(
        candidates = emptyList(),
        generationCount = 0,
        remainingGenerationCount = 3,
        signupState = SignupState.PROFILE_IMAGE_REQUIRED
    ),
    private val generatedImages: ProfileImageCandidates = profileImages
) : AuthGateway {
    var lastSignupInput: SignupInput? = null
    var profileListRequests: Int = 0
    var profileGenerationRequests: Int = 0
    var selectedProfileImageId: Long? = null

    override suspend fun login(identity: IdentityToken): LoginResult = loginResult

    override suspend fun nicknameCandidates(): NicknameCandidateResponse = NicknameCandidateResponse(
        selectionToken = "selection-token",
        candidates = listOf(
            "따스한 사슴 3492",
            "잔잔한 거북이 1108",
            "호기심 많은 너구리 9027"
        ).map { NicknameCandidate(it) }
    )

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession {
        lastSignupInput = input
        return ServiceSession("access", "refresh", SignupState.PROFILE_IMAGE_REQUIRED)
    }

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates {
        profileListRequests += 1
        return profileImages
    }

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates {
        profileGenerationRequests += 1
        return generatedImages
    }

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult {
        selectedProfileImageId = profileImageId
        return ProfileImageSelectionResult(
            selectedImage = ProfileImageCandidate(profileImageId, "https://cdn.example/$profileImageId.png", true),
            signupState = SignupState.SIGNUP_COMPLETE
        )
    }
}
