package kr.hanchae.moyeotrip.data.auth

import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.AuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.LoginResult
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidateResponse
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidates
import kr.hanchae.moyeotrip.domain.auth.ProfileImageSelectionResult
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupInput

class RefreshingAuthGateway(private val delegate: AuthGateway, private val sessionStore: AuthSessionStore) :
    AuthGateway {
    override suspend fun login(identity: IdentityToken): LoginResult = delegate.login(identity)

    override suspend fun nicknameCandidates(): NicknameCandidateResponse = delegate.nicknameCandidates()

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession =
        delegate.signup(identity, input)

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates =
        authorized(accessToken, delegate::profileImages)

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates =
        authorized(accessToken, delegate::generateProfileImage)

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult =
        authorized(accessToken) { token ->
            delegate.selectProfileImage(token, profileImageId)
        }

    override suspend fun linkedProviders(accessToken: String): Set<AuthProvider> =
        authorized(accessToken, delegate::linkedProviders)

    override suspend fun linkProvider(accessToken: String, identity: IdentityToken): Set<AuthProvider> =
        authorized(accessToken) { token -> delegate.linkProvider(token, identity) }

    override suspend fun refresh(refreshToken: String): ServiceSession = delegate.refresh(refreshToken)

    override suspend fun withdraw(accessToken: String) {
        authorized(accessToken) { token -> delegate.withdraw(token) }
    }

    private suspend fun <T> authorized(accessToken: String, request: suspend (String) -> T): T = try {
        request(accessToken)
    } catch (error: AuthApiException) {
        // 가입 게이트(40902·40918)와 "이미 설정 완료"(40919)는 토큰 문제가 아니다.
        // 재발급 경로로 흘리면 새 토큰으로 같은 409 를 받아 무한 재시도가 된다(정본 R1).
        // 상태 코드보다 오류 코드를 먼저 본다 — 서버가 401 로 감싸 보내도 여기서 멈춰야 한다.
        if (error.signupGate != null || error.profileImageAlreadySet) throw error
        if (error.statusCode != 401) throw error
        val session = sessionStore.current
        val refreshToken = session.refreshToken ?: throw error
        val provider = session.provider ?: throw error
        val refreshed = delegate.refresh(refreshToken)
        sessionStore.saveSignup(provider, refreshed)
        request(refreshed.accessToken)
    }
}
