package kr.hanchae.moyeotrip

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.auth.AuthApiException
import kr.hanchae.moyeotrip.data.auth.KakaoCustomTokenClient
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KakaoCustomTokenClientTest {
    @Test
    fun exchangesKakaoAccessTokenForFirebaseCustomToken() = runBlocking {
        val connection = FakeHttpURLConnection(
            configuredResponseCode = 200,
            responseBody = """{"customToken":"firebase-custom-token"}"""
        )
        val client = KakaoCustomTokenClient("https://api.example.com/") { url ->
            assertEquals(
                "https://api.example.com/api/v1/auth/firebase/kakao/custom-token",
                url.toString()
            )
            connection
        }

        val customToken = client.exchange("kakao-access-token")

        assertEquals("firebase-custom-token", customToken)
        assertEquals("POST", connection.requestMethod)
        assertEquals(
            "kakao-access-token",
            JSONObject(connection.requestBody()).getString("accessToken")
        )
        assertTrue(connection.disconnected)
    }

    @Test
    fun preservesBackendErrorMessage() = runBlocking {
        val connection = FakeHttpURLConnection(
            configuredResponseCode = 401,
            errorBody = """{"message":"카카오 토큰이 만료되었어요."}"""
        )
        val client = KakaoCustomTokenClient("https://api.example.com") { connection }

        val error = runCatching { client.exchange("expired-token") }.exceptionOrNull()

        assertTrue(error is AuthApiException)
        assertEquals(401, (error as AuthApiException).statusCode)
        assertEquals("카카오 토큰이 만료되었어요.", error.message)
        assertTrue(connection.disconnected)
    }
}

private class FakeHttpURLConnection(
    url: URL = URL("https://api.example.com"),
    private val configuredResponseCode: Int,
    private val responseBody: String = "",
    private val errorBody: String = ""
) : HttpURLConnection(url) {
    private val requestOutput = ByteArrayOutputStream()
    var disconnected = false
        private set

    override fun getOutputStream() = requestOutput

    override fun getResponseCode() = configuredResponseCode

    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBody.toByteArray())

    override fun getErrorStream(): InputStream = ByteArrayInputStream(errorBody.toByteArray())

    override fun disconnect() {
        disconnected = true
    }

    override fun usingProxy() = false

    override fun connect() = Unit

    fun requestBody() = requestOutput.toString(Charsets.UTF_8.name())
}
