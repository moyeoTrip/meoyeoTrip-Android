package kr.hanchae.moyeotrip.data.auth

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.Gender
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.SignupInput
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class HttpAuthGatewayTest {
    @Test
    fun everyProviderUsesUnifiedLoginEndpoint() = runBlocking {
        AuthProvider.entries.forEach { provider ->
            val connection = JsonConnection(
                URL("https://example.test/api/v1/auth/login"),
                """{"accessToken":"access","refreshToken":"refresh","isNewUser":false,"signupState":"SIGNUP_COMPLETE","providerType":"${provider.name}"}"""
            )
            val gateway = HttpAuthGateway("https://example.test") { url ->
                assertEquals("/api/v1/auth/login", url.path)
                connection
            }

            gateway.login(IdentityToken(provider, "firebase-id-token"))

            assertEquals("POST", connection.requestMethod)
        }
    }

    @Test
    fun everyProviderUsesUnifiedSignupEndpoint() = runBlocking {
        AuthProvider.entries.forEach { provider ->
            val connection = JsonConnection(
                URL("https://example.test/api/v1/auth/signup"),
                """{"accessToken":"access","refreshToken":"refresh","signupState":"PROFILE_IMAGE_REQUIRED"}"""
            )
            val gateway = HttpAuthGateway("https://example.test") { url ->
                assertEquals("/api/v1/auth/signup", url.path)
                connection
            }

            gateway.signup(
                IdentityToken(provider, "firebase-id-token"),
                SignupInput("selection", "따스한 사슴 3492", Gender.FEMALE, "1998-04-12")
            )

            assertEquals("POST", connection.requestMethod)
        }
    }

    @Test
    fun connectedProvidersAreReadFromProtectedEndpoint() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/auth/providers"),
            """{"providers":["KAKAO","GOOGLE"]}"""
        )
        val gateway = HttpAuthGateway("https://example.test") { connection }

        val providers = gateway.linkedProviders("access-token")

        assertEquals(setOf(AuthProvider.KAKAO, AuthProvider.GOOGLE), providers)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
    }

    @Test
    fun withdrawAcceptsNoContentWithoutReadingResponseBody() = runBlocking {
        val connection = NoContentConnection(URL("https://example.test/api/v1/users/me"))
        val gateway = HttpAuthGateway("https://example.test") { connection }

        gateway.withdraw("access-token")

        assertEquals("DELETE", connection.requestMethod)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
    }
}

private class JsonConnection(url: URL, response: String) : HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()
    val requestBody = ByteArrayOutputStream()

    override fun disconnect() = Unit
    override fun usingProxy(): Boolean = false
    override fun connect() = Unit
    override fun getResponseCode(): Int = HTTP_OK
    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBytes)
    override fun getOutputStream() = requestBody
}

private class NoContentConnection(url: URL) : HttpURLConnection(url) {
    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = HTTP_NO_CONTENT

    override fun getInputStream(): InputStream {
        fail("204 응답에서는 본문을 읽으면 안 됩니다.")
        error("unreachable")
    }
}
