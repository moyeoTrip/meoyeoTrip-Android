package kr.hanchae.moyeotrip.data.api

import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class MoyeoApiException(val statusCode: Int, override val message: String) : Exception(message)

/**
 * 보호 API 공용 HTTP 클라이언트 — HttpAuthGateway·HttpTourismContentRepository 와 같은
 * HttpURLConnection + org.json 조합을 그대로 따른다. 401이면 [refreshAccessToken] 으로
 * 한 번 재발급을 시도한 뒤 같은 요청을 다시 보낸다.
 */
class MoyeoApiClient(
    baseUrl: String,
    private val accessToken: () -> String?,
    private val refreshAccessToken: suspend () -> String? = { null },
    private val connectionFactory: (URL) -> HttpURLConnection = { url ->
        url.openConnection() as HttpURLConnection
    }
) {
    private val rootUrl = baseUrl.trimEnd('/')

    suspend fun getObject(path: String): JSONObject = JSONObject(request("GET", path))

    suspend fun getArray(path: String): JSONArray = JSONArray(request("GET", path))

    suspend fun sendForObject(method: String, path: String, body: JSONObject? = null): JSONObject {
        val text = request(method, path, body)
        return if (text.isBlank()) JSONObject() else JSONObject(text)
    }

    suspend fun send(method: String, path: String, body: JSONObject? = null) {
        request(method, path, body)
    }

    private suspend fun request(method: String, path: String, body: JSONObject? = null): String {
        val token = accessToken()
        return try {
            execute(method, path, body, token)
        } catch (error: MoyeoApiException) {
            if (error.statusCode != 401) throw error
            val refreshed = refreshAccessToken() ?: throw error
            execute(method, path, body, refreshed)
        }
    }

    private suspend fun execute(method: String, path: String, body: JSONObject?, token: String?): String =
        withContext(Dispatchers.IO) {
            val connection = connectionFactory(URL("$rootUrl$path"))
            try {
                connection.requestMethod = method
                connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
                connection.readTimeout = READ_TIMEOUT_MILLIS
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (!token.isNullOrBlank()) {
                    connection.setRequestProperty("Authorization", "Bearer $token")
                }
                if (body != null) {
                    connection.doOutput = true
                    connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write(body.toString())
                    }
                }

                val statusCode = connection.responseCode
                val responseText = if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    ""
                } else {
                    connection.responseStream(statusCode)?.readTextSafely().orEmpty()
                }
                if (statusCode !in 200..299) {
                    throw MoyeoApiException(statusCode, responseText.apiErrorMessage(statusCode))
                }
                responseText
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 8_000
        private const val READ_TIMEOUT_MILLIS = 15_000
    }
}

private fun HttpURLConnection.responseStream(statusCode: Int): InputStream? =
    if (statusCode in 200..299) inputStream else errorStream

private fun InputStream.readTextSafely(): String = bufferedReader().use(BufferedReader::readText)

private fun String.apiErrorMessage(statusCode: Int): String {
    if (isBlank()) return "서버 요청에 실패했어요. ($statusCode)"
    val json = runCatching { JSONObject(this) }.getOrNull()
    return json?.stringOrNull("errorMessage")
        ?: json?.stringOrNull("message")
        ?: json?.stringOrNull("detail")
        ?: json?.stringOrNull("error")
        ?: "서버 요청에 실패했어요. ($statusCode)"
}

internal fun JSONObject.stringOrNull(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeIf(String::isNotBlank)

internal fun JSONObject.intOrNull(key: String): Int? = if (!has(key) || isNull(key)) null else optInt(key)

internal fun JSONObject.longOrNull(key: String): Long? = if (!has(key) || isNull(key)) null else optLong(key)

internal fun JSONObject.doubleOrNull(key: String): Double? =
    if (!has(key) || isNull(key)) null else optDouble(key).takeUnless(Double::isNaN)

internal fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

internal fun <T> JSONObject.mapArray(key: String, transform: (JSONObject) -> T): List<T> =
    optJSONArray(key)?.mapObjects(transform).orEmpty()
