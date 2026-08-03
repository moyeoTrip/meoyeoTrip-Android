package kr.hanchae.moyeotrip.data.auth

import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

fun interface KakaoCustomTokenExchanger {
    suspend fun exchange(kakaoAccessToken: String): String
}

class KakaoCustomTokenClient(
    baseUrl: String,
    private val connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }
) : KakaoCustomTokenExchanger {
    private val endpoint = "${baseUrl.trimEnd('/')}/api/v1/auth/firebase/kakao/custom-token"

    override suspend fun exchange(kakaoAccessToken: String): String = withContext(Dispatchers.IO) {
        val connection = connectionFactory(URL(endpoint))
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
            connection.readTimeout = READ_TIMEOUT_MILLIS
            connection.doOutput = true
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(JSONObject().put("accessToken", kakaoAccessToken).toString())
            }

            val statusCode = connection.responseCode
            val responseText = connection.responseStream(statusCode)?.readTextSafely().orEmpty()
            if (statusCode !in 200..299) {
                throw AuthApiException(statusCode, responseText.apiErrorMessage(statusCode))
            }
            JSONObject(responseText).getString("customToken")
        } finally {
            connection.disconnect()
        }
    }

    private fun HttpURLConnection.responseStream(statusCode: Int): InputStream? =
        if (statusCode in 200..299) inputStream else errorStream

    private fun InputStream.readTextSafely(): String = bufferedReader().use(BufferedReader::readText)

    private fun String.apiErrorMessage(statusCode: Int): String {
        val json = takeIf(String::isNotBlank)?.let { runCatching { JSONObject(it) }.getOrNull() }
        return json?.optString("message")?.takeIf(String::isNotBlank)
            ?: json?.optString("detail")?.takeIf(String::isNotBlank)
            ?: json?.optString("error")?.takeIf(String::isNotBlank)
            ?: "카카오 인증 토큰을 교환하지 못했어요. ($statusCode)"
    }

    private companion object {
        const val CONNECT_TIMEOUT_MILLIS = 8_000
        const val READ_TIMEOUT_MILLIS = 20_000
    }
}
