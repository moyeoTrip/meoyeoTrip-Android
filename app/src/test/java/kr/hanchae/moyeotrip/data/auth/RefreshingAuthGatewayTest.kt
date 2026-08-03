package kr.hanchae.moyeotrip.data.auth

import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.domain.auth.AuthGateway
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.InMemoryAuthSessionStore
import kr.hanchae.moyeotrip.domain.auth.LoginResult
import kr.hanchae.moyeotrip.domain.auth.NicknameCandidateResponse
import kr.hanchae.moyeotrip.domain.auth.ProfileImageCandidates
import kr.hanchae.moyeotrip.domain.auth.ProfileImageSelectionResult
import kr.hanchae.moyeotrip.domain.auth.ServiceSession
import kr.hanchae.moyeotrip.domain.auth.SignupInput
import kr.hanchae.moyeotrip.domain.auth.SignupState
import org.junit.Assert.assertEquals
import org.junit.Test

class RefreshingAuthGatewayTest {
    @Test
    fun protectedProviderRequestRefreshesRotatesAndRetriesOnce() = runBlocking {
        val store = InMemoryAuthSessionStore().apply {
            saveSignup(
                AuthProvider.KAKAO,
                ServiceSession("expired-access", "old-refresh", SignupState.SIGNUP_COMPLETE)
            )
        }
        val delegate = ExpiringGateway()
        val gateway = RefreshingAuthGateway(delegate, store)

        val providers = gateway.linkedProviders("expired-access")

        assertEquals(setOf(AuthProvider.KAKAO, AuthProvider.GOOGLE), providers)
        assertEquals(listOf("expired-access", "new-access"), delegate.providerTokens)
        assertEquals("new-access", store.current.accessToken)
        assertEquals("new-refresh", store.current.refreshToken)
        assertEquals(1, delegate.refreshCalls)
    }
}

private class ExpiringGateway : AuthGateway {
    val providerTokens = mutableListOf<String>()
    var refreshCalls = 0

    override suspend fun linkedProviders(accessToken: String): Set<AuthProvider> {
        providerTokens += accessToken
        if (accessToken == "expired-access") throw AuthApiException(401, "expired")
        return setOf(AuthProvider.KAKAO, AuthProvider.GOOGLE)
    }

    override suspend fun refresh(refreshToken: String): ServiceSession {
        refreshCalls += 1
        return ServiceSession("new-access", "new-refresh", SignupState.SIGNUP_COMPLETE)
    }

    override suspend fun login(identity: IdentityToken): LoginResult = error("unused")
    override suspend fun nicknameCandidates(): NicknameCandidateResponse = error("unused")
    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession = error("unused")
    override suspend fun profileImages(accessToken: String): ProfileImageCandidates = error("unused")
    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates = error("unused")
    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult =
        error("unused")

    override suspend fun linkProvider(accessToken: String, identity: IdentityToken): Set<AuthProvider> = error("unused")
    override suspend fun withdraw(accessToken: String) = error("unused")
}
