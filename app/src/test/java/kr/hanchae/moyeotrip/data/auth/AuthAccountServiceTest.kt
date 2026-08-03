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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAccountServiceTest {
    @Test
    fun withdrawDeletesAccountAndClearsLocalSessions() = runBlocking {
        val store = authenticatedStore()
        val gateway = AccountGateway()
        var providersCleared = false
        val service = AuthAccountService(
            authGateway = gateway,
            sessionStore = store,
            clearProviderSessions = { providersCleared = true }
        )

        service.withdraw()

        assertEquals(listOf("access-token"), gateway.withdrawnAccessTokens)
        assertNull(store.current.accessToken)
        assertTrue(providersCleared)
    }

    @Test
    fun withdrawRefreshesExpiredAccessTokenOnce() = runBlocking {
        val store = authenticatedStore()
        val gateway = AccountGateway(withdrawErrors = ArrayDeque(listOf(AuthApiException(401, "expired"))))
        val service = AuthAccountService(gateway, store)

        service.withdraw()

        assertEquals("refresh-token", gateway.capturedRefreshToken)
        assertEquals(listOf("access-token", "refreshed-access"), gateway.withdrawnAccessTokens)
        assertNull(store.current.accessToken)
    }

    @Test
    fun alreadyDeletedAccountStillClearsLocalSessions() = runBlocking {
        val store = authenticatedStore()
        val gateway = AccountGateway(withdrawErrors = ArrayDeque(listOf(AuthApiException(404, "missing"))))
        val service = AuthAccountService(gateway, store)

        service.withdraw()

        assertNull(store.current.accessToken)
    }

    private fun authenticatedStore() = InMemoryAuthSessionStore().apply {
        saveSignup(
            AuthProvider.KAKAO,
            ServiceSession("access-token", "refresh-token", SignupState.SIGNUP_COMPLETE)
        )
    }
}

private class AccountGateway(private val withdrawErrors: ArrayDeque<AuthApiException> = ArrayDeque()) : AuthGateway {
    val withdrawnAccessTokens = mutableListOf<String>()
    var capturedRefreshToken: String? = null

    override suspend fun withdraw(accessToken: String) {
        withdrawnAccessTokens += accessToken
        withdrawErrors.removeFirstOrNull()?.let { throw it }
    }

    override suspend fun refresh(refreshToken: String): ServiceSession {
        capturedRefreshToken = refreshToken
        return ServiceSession("refreshed-access", "refreshed-refresh", SignupState.SIGNUP_COMPLETE)
    }

    override suspend fun login(identity: IdentityToken): LoginResult = error("Not used")

    override suspend fun nicknameCandidates(): NicknameCandidateResponse = error("Not used")

    override suspend fun signup(identity: IdentityToken, input: SignupInput): ServiceSession = error("Not used")

    override suspend fun profileImages(accessToken: String): ProfileImageCandidates = error("Not used")

    override suspend fun generateProfileImage(accessToken: String): ProfileImageCandidates = error("Not used")

    override suspend fun selectProfileImage(accessToken: String, profileImageId: Long): ProfileImageSelectionResult =
        error("Not used")
}
