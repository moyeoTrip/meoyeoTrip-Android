package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.delay

fun interface IdentityTokenProvider {
    suspend fun acquire(provider: AuthProvider): IdentityToken

    suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken = error("이메일 로그인을 사용할 수 없어요.")

    suspend fun sendPasswordReset(email: String) {
        error("비밀번호 재설정을 사용할 수 없어요.")
    }
}

interface AuthGateway {
    suspend fun login(identity: IdentityToken): LoginResult

    suspend fun nicknameCandidates(): NicknameCandidateResponse

    suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession

    suspend fun profileImages(accessToken: String): ProfileImageCandidates

    suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates

    suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult

    suspend fun linkedProviders(accessToken: String): Set<AuthProvider> = error("로그인 방식을 조회할 수 없어요.")

    suspend fun linkProvider(accessToken: String, identity: IdentityToken): Set<AuthProvider> =
        error("로그인 방식을 연결할 수 없어요.")

    suspend fun refresh(refreshToken: String): ServiceSession = error("세션을 갱신할 수 없어요.")

    suspend fun withdraw(accessToken: String) {
        error("계정을 탈퇴할 수 없어요.")
    }
}

class MockIdentityTokenProvider(private val fcmToken: String? = "demo-fcm-token") : IdentityTokenProvider {
    override suspend fun acquire(provider: AuthProvider): IdentityToken {
        delay(350)
        return IdentityToken(
            provider = provider,
            idToken = "demo-${provider.pathValue}-firebase-id-token",
            fcmToken = fcmToken
        )
    }

    override suspend fun acquireEmail(request: EmailAuthRequest): IdentityToken {
        require(request.email.contains('@')) { "이메일 주소를 확인해 주세요." }
        require(request.password.length >= 6) { "비밀번호는 6자 이상 입력해 주세요." }
        delay(350)
        return IdentityToken(
            provider = AuthProvider.EMAIL,
            idToken = "demo-email-firebase-id-token",
            fcmToken = fcmToken
        )
    }

    override suspend fun sendPasswordReset(email: String) {
        require(email.contains('@')) { "이메일 주소를 확인해 주세요." }
        delay(250)
    }
}

class DemoAuthGateway(private val existingUserProviders: Set<AuthProvider> = emptySet()) : AuthGateway {
    private val nicknameGateway = MockNicknameCandidateGateway()
    private var generatedImageCount = 0
    private var selectedImageId: Long? = null
    private val linkedProviders = existingUserProviders.toMutableSet()

    override suspend fun login(identity: IdentityToken): LoginResult {
        delay(450)
        val existingUser = identity.provider in existingUserProviders
        return LoginResult(
            accessToken = if (existingUser) "demo-access-token" else null,
            refreshToken = if (existingUser) "demo-refresh-token" else null,
            isNewUser = !existingUser,
            signupState = if (existingUser) SignupState.SIGNUP_COMPLETE else SignupState.USER_INFO_REQUIRED,
            providerType = identity.provider
        )
    }

    override suspend fun nicknameCandidates(): NicknameCandidateResponse = nicknameGateway.fetchCandidates()

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession {
        require(input.nicknameSelectionToken.isNotBlank() && input.nickname.isNotBlank())
        require(input.birthDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        delay(500)
        return ServiceSession(
            accessToken = "demo-access-token",
            refreshToken = "demo-refresh-token",
            signupState = SignupState.PROFILE_IMAGE_REQUIRED
        )
    }

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates {
        delay(250)
        return profileResponse()
    }

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates {
        delay(600)
        if (generatedImageCount >= MAX_PROFILE_GENERATIONS) {
            error("프로필 이미지는 최대 3회까지 생성할 수 있어요.")
        }
        generatedImageCount += 1
        return profileResponse()
    }

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult {
        delay(350)
        selectedImageId = profileImageId
        val selected = profileCandidates().first { it.profileImageId == profileImageId }.copy(selected = true)
        return ProfileImageSelectionResult(selected, SignupState.SIGNUP_COMPLETE)
    }

    override suspend fun linkedProviders(accessToken: String): Set<AuthProvider> = linkedProviders.toSet()

    override suspend fun linkProvider(accessToken: String, identity: IdentityToken): Set<AuthProvider> {
        delay(250)
        linkedProviders += identity.provider
        return linkedProviders.toSet()
    }

    override suspend fun refresh(refreshToken: String): ServiceSession {
        delay(120)
        return ServiceSession(
            accessToken = "demo-access-token-refreshed",
            refreshToken = "demo-refresh-token-refreshed",
            signupState = SignupState.SIGNUP_COMPLETE
        )
    }

    override suspend fun withdraw(accessToken: String) {
        delay(120)
    }

    private fun profileResponse(): ProfileImageCandidates = ProfileImageCandidates(
        candidates = profileCandidates(),
        generationCount = generatedImageCount,
        remainingGenerationCount = MAX_PROFILE_GENERATIONS - generatedImageCount,
        signupState = SignupState.PROFILE_IMAGE_REQUIRED
    )

    private fun profileCandidates(): List<ProfileImageCandidate> = (1..generatedImageCount).map { index ->
        val id = index.toLong()
        ProfileImageCandidate(
            profileImageId = id,
            profileImageUrl = "demo://profile/$id",
            selected = selectedImageId == id
        )
    }

    companion object {
        private const val MAX_PROFILE_GENERATIONS = 3
    }
}
