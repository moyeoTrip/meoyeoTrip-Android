package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.auth.AuthApiException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFlowCoordinatorTest {
    @Test
    fun storedCompleteSessionIsValidatedAndRefreshedAtStartup() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.GOOGLE,
                ServiceSession("old-access", "old-refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val gateway = FakeAuthGateway(
            refreshResult = ServiceSession("new-access", "new-refresh", SignupState.SIGNUP_COMPLETE)
        )
        val coordinator = coordinator(gateway, store)

        coordinator.restoreSession()

        assertEquals(AuthDestination.COMPLETE, coordinator.state.destination)
        assertEquals("old-refresh", gateway.lastRefreshToken)
        assertEquals("new-access", store.current.accessToken)
        assertEquals("new-refresh", store.current.refreshToken)
    }

    @Test
    fun storedIncompleteSessionResumesAtServerProfileImageStep() = runBlocking {
        val images = profileImages(
            listOf(ProfileImageCandidate(21, "https://cdn.example/21.png", true)),
            generationCount = 1,
            remainingCount = 2
        )
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("old-access", "old-refresh", SignupState.PROFILE_IMAGE_REQUIRED)
            )
        }
        val gateway = FakeAuthGateway(
            profileImages = images,
            refreshResult = ServiceSession("new-access", "new-refresh", SignupState.PROFILE_IMAGE_REQUIRED)
        )
        val coordinator = coordinator(gateway, store)

        coordinator.restoreSession()

        assertEquals(AuthDestination.PROFILE_IMAGE, coordinator.state.destination)
        assertEquals(21L, coordinator.state.selectedProfileImageId)
        assertEquals(1, gateway.profileListRequests)
    }

    /**
     * 40902 복귀(정본 R2-1). 세션은 살아 있으니 재로그인을 요구하지 않는다 —
     * 기기에 남아 있는 Firebase 로그인으로 새 idToken 을 받아 회원 정보 입력으로 이어간다.
     */
    @Test
    fun restoredSignupResumesWithFreshFirebaseTokenWithoutReLogin() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("old-access", "old-refresh", SignupState.USER_INFO_REQUIRED)
            )
        }
        val gateway = FakeAuthGateway(
            refreshResult = ServiceSession("new-access", "new-refresh", SignupState.USER_INFO_REQUIRED)
        )
        val provider = RecordingIdentityTokenProvider()
        val coordinator = AuthFlowCoordinator(provider, gateway, store)

        coordinator.restoreSession()

        assertEquals(AuthDestination.NICKNAME, coordinator.state.destination)
        assertEquals(AuthProvider.KAKAO, provider.lastCurrentIdentityProvider)
        assertEquals("firebase-current-id-token", coordinator.state.identity?.idToken)
        assertEquals(3, coordinator.state.nickname.candidates.size)
        // 로그인 화면을 다시 띄우지 않았으므로 세션도 지우지 않는다.
        assertEquals("new-refresh", store.current.refreshToken)
        assertNull(provider.lastProvider)
    }

    @Test
    fun restoredSignupFallsBackToLoginOnlyWhenFirebaseUserIsGone() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.GOOGLE,
                ServiceSession("old-access", "old-refresh", SignupState.USER_INFO_REQUIRED)
            )
        }
        val gateway = FakeAuthGateway(
            refreshResult = ServiceSession("new-access", "new-refresh", SignupState.USER_INFO_REQUIRED)
        )
        val provider = RecordingIdentityTokenProvider(currentIdentityToken = null)
        val coordinator = AuthFlowCoordinator(provider, gateway, store)

        coordinator.restoreSession()

        assertEquals(AuthDestination.LOGIN, coordinator.state.destination)
        assertNull(store.current.refreshToken)
        assertNull(coordinator.state.errorMessage)
    }

    /** 세션 만료(`400 40001`)는 정상 흐름이다 — 로그인 화면으로 보내되 오류 배너를 남기지 않는다(정본 R3·R4). */
    @Test
    fun expiredRefreshTokenReturnsToLoginWithoutErrorBanner() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("old-access", "old-refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val gateway = FailingRefreshGateway(
            AuthApiException(400, "유효하지 않은 RefreshToken 입니다.", errorCode = 40001)
        )
        val coordinator = coordinator(gateway, store)

        coordinator.restoreSession()

        assertEquals(AuthDestination.LOGIN, coordinator.state.destination)
        assertNull(coordinator.state.errorMessage)
        assertFalse(coordinator.state.isLoading)
        assertNull(store.current.refreshToken)
    }

    /** 서버 5xx 는 사용자가 다시 시도할 수 있는 실패다 — 계속 오류로 보여준다(정본 §3). */
    @Test
    fun serverFailureDuringRestoreStillShowsError() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("old-access", "old-refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val gateway = FailingRefreshGateway(AuthApiException(500, "서버 오류입니다.", errorCode = null))
        val coordinator = coordinator(gateway, store)

        coordinator.restoreSession()

        assertEquals("서버 오류입니다.", coordinator.state.errorMessage)
        assertEquals("old-refresh", store.current.refreshToken)
    }

    /** 소셜 로그인 취소는 실패가 아니다 — 로딩만 걷고 로그인 화면을 그대로 둔다(정본 R1·R2). */
    @Test
    fun cancelledSocialLoginLeavesLoginScreenUntouched() = runBlocking {
        val provider = RecordingIdentityTokenProvider(cancelAcquire = true)
        val coordinator = AuthFlowCoordinator(provider, FakeAuthGateway(), InMemoryAuthSessionStore())

        coordinator.login(AuthProvider.KAKAO)

        assertEquals(AuthDestination.LOGIN, coordinator.state.destination)
        assertNull(coordinator.state.errorMessage)
        assertFalse(coordinator.state.isLoading)
    }

    @Test
    fun startupWithoutStoredRefreshTokenDoesNotCallServer() = runBlocking {
        val gateway = FakeAuthGateway()
        val coordinator = coordinator(gateway, InMemoryAuthSessionStore())

        coordinator.restoreSession()

        assertNull(gateway.lastRefreshToken)
        assertEquals(AuthDestination.LOGIN, coordinator.state.destination)
    }

    @Test
    fun newUserFollowsNicknameInfoSignupAndProfileFlow() = runBlocking {
        val gateway = FakeAuthGateway()
        val sessionStore = InMemoryAuthSessionStore()
        val coordinator = coordinator(gateway, sessionStore)

        coordinator.login(AuthProvider.KAKAO)

        assertEquals(AuthDestination.NICKNAME, coordinator.state.destination)
        assertEquals(3, coordinator.state.nickname.candidates.size)
        coordinator.selectNickname("따스한 사슴 3492")
        coordinator.signup(Gender.FEMALE, "1998-04-12", listOf(1L, 2L))

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
    fun successfulBackendLoginMarksFcmTokenRegistered() = runBlocking {
        val registered = mutableListOf<String>()
        val gateway = FakeAuthGateway(
            loginResult = LoginResult(
                accessToken = null,
                refreshToken = null,
                isNewUser = true,
                signupState = SignupState.USER_INFO_REQUIRED,
                providerType = AuthProvider.GOOGLE
            )
        )
        val coordinator = AuthFlowCoordinator(
            identityTokenProvider = RecordingIdentityTokenProvider(),
            authGateway = gateway,
            sessionStore = InMemoryAuthSessionStore(),
            onFcmTokenRegistered = registered::add
        )

        coordinator.login(AuthProvider.GOOGLE)

        assertEquals(listOf("fcm-token"), registered)
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
        val request = emailRequest()

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

    private fun emailRequest() = EmailAuthRequest(
        email = "trip@example.com",
        password = "password123"
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

private class RecordingIdentityTokenProvider(
    /** 기기에 남아 있는 Firebase 로그인. null 이면 로그아웃된 기기다(정본 R2-1). */
    private val currentIdentityToken: String? = "firebase-current-id-token",
    private val cancelAcquire: Boolean = false
) : IdentityTokenProvider {
    var lastProvider: AuthProvider? = null
    var lastCurrentIdentityProvider: AuthProvider? = null
    var lastEmailRequest: EmailAuthRequest? = null
    var lastResetEmail: String? = null

    override suspend fun acquire(provider: AuthProvider): IdentityToken {
        if (cancelAcquire) throw SocialLoginCancelledException()
        lastProvider = provider
        return IdentityToken(provider, "firebase-id-token", "fcm-token")
    }

    override suspend fun currentIdentity(provider: AuthProvider): IdentityToken? {
        lastCurrentIdentityProvider = provider
        return currentIdentityToken?.let { IdentityToken(provider, it, "fcm-token") }
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
    private val generatedImages: ProfileImageCandidates = profileImages,
    private val refreshResult: ServiceSession = ServiceSession(
        "refreshed-access",
        "refreshed-refresh",
        SignupState.SIGNUP_COMPLETE
    )
) : AuthGateway {
    var lastSignupInput: SignupInput? = null
    var profileListRequests: Int = 0
    var profileGenerationRequests: Int = 0
    var selectedProfileImageId: Long? = null
    var lastRefreshToken: String? = null

    override suspend fun login(identity: IdentityToken): LoginResult = loginResult

    override suspend fun refresh(refreshToken: String): ServiceSession {
        lastRefreshToken = refreshToken
        return refreshResult
    }

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

/** 세션 복원의 재발급만 실패시키는 게이트웨이. 만료(400)와 서버 오류(500)를 갈라 보려고 쓴다. */
private class FailingRefreshGateway(private val error: Throwable) : AuthGateway {
    override suspend fun login(identity: IdentityToken): LoginResult {
        error("이 테스트는 로그인을 부르지 않는다.")
    }

    override suspend fun refresh(refreshToken: String): ServiceSession = throw error

    override suspend fun nicknameCandidates(): NicknameCandidateResponse {
        error("이 테스트는 닉네임 후보를 부르지 않는다.")
    }

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession {
        error("이 테스트는 가입을 부르지 않는다.")
    }

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates {
        error("이 테스트는 프로필 이미지를 부르지 않는다.")
    }

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates {
        error("이 테스트는 프로필 이미지 생성을 부르지 않는다.")
    }

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult {
        error("이 테스트는 프로필 이미지 선택을 부르지 않는다.")
    }
}
