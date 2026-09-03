package kr.hanchae.moyeotrip.domain.auth

fun interface IdentityTokenProvider {
    suspend fun acquire(provider: AuthProvider): IdentityToken

    /**
     * 기기에 남아 있는 Firebase 로그인으로 **재로그인 없이** 새 idToken 을 받는다. 없으면 null.
     *
     * 세션을 복원했는데 가입이 안 끝나 있으면(`USER_INFO_REQUIRED`) `POST /auth/signup` 에 낼
     * idToken 이 앱에 없다. Firebase SDK 는 로그인 상태를 기기에 유지하므로 여기서 새로 받아
     * 가입을 그대로 이어갈 수 있다 — 사용자에게 다시 로그인하라고 하지 않는다(정본 R2-1).
     */
    suspend fun currentIdentity(provider: AuthProvider): IdentityToken? = null

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
