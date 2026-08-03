package kr.hanchae.moyeotrip.data.auth

import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.AuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.EmailAuthRequest
import kr.hanchae.moyeotrip.domain.auth.IdentityTokenProvider

class AuthAccountService(
    private val authGateway: AuthGateway,
    private val sessionStore: AuthSessionStore,
    private val identityTokenProvider: IdentityTokenProvider? = null,
    private val clearProviderSessions: suspend () -> Unit = {},
    private val clearUserProfile: () -> Unit = {}
) {
    suspend fun logout() {
        sessionStore.clear()
        clearUserProfile()
        clearProviderSessions()
    }

    suspend fun linkedProviders(): Set<AuthProvider> {
        val accessToken = sessionStore.current.accessToken ?: error("로그인 정보가 없어요. 다시 로그인해 주세요.")
        return authGateway.linkedProviders(accessToken)
    }

    suspend fun linkProvider(provider: AuthProvider, emailRequest: EmailAuthRequest? = null): Set<AuthProvider> {
        val tokenProvider = identityTokenProvider ?: error("로그인 제공자를 연결할 수 없어요.")
        val identity = if (provider == AuthProvider.EMAIL) {
            tokenProvider.acquireEmail(emailRequest ?: error("이메일과 비밀번호를 입력해 주세요."))
        } else {
            tokenProvider.acquire(provider)
        }
        require(identity.provider == provider) { "연결할 로그인 제공자 정보가 일치하지 않아요." }
        val accessToken = sessionStore.current.accessToken ?: error("로그인 정보가 없어요. 다시 로그인해 주세요.")
        return authGateway.linkProvider(accessToken, identity)
    }

    suspend fun withdraw() {
        val initialSession = sessionStore.current
        val accessToken = initialSession.accessToken ?: error("로그인 정보가 없어요. 다시 로그인해 주세요.")

        try {
            authGateway.withdraw(accessToken)
        } catch (error: AuthApiException) {
            when (error.statusCode) {
                401 -> {
                    val refreshToken = initialSession.refreshToken
                        ?: error("로그인 정보가 만료됐어요. 다시 로그인해 주세요.")
                    val provider = initialSession.provider
                        ?: error("로그인 방식을 확인하지 못했어요. 다시 로그인해 주세요.")
                    val refreshed = authGateway.refresh(refreshToken)
                    sessionStore.saveSignup(provider, refreshed)
                    authGateway.withdraw(refreshed.accessToken)
                }

                404 -> {
                    clearLocalSessions()
                    return
                }

                else -> throw error
            }
        }

        clearLocalSessions()
    }

    private suspend fun clearLocalSessions() {
        sessionStore.clear()
        clearUserProfile()
        clearProviderSessions()
    }
}
