package kr.hanchae.moyeotrip.data.api

import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * 서버 오류 응답 — 본문은 `{"code": 40915, "errorMessage": "…"}` 형태다.
 * [statusCode] 만으로는 갈라지지 않는 계약이 있어([errorCode] 예: `409 40915` = 아직 완료되지 않은 여행)
 * 오류 코드도 같이 들고 다닌다.
 */
class MoyeoApiException(val statusCode: Int, override val message: String, val errorCode: Int? = null) :
    Exception(message) {
    /** `GET /chat-rooms/{id}/companions` 는 완료 여행 전용이다 — 권한 오류가 아니라 "아직 여행 전"이다. */
    val tripNotCompleted: Boolean get() = statusCode == 409 && errorCode == TRIP_NOT_COMPLETED_CODE

    companion object {
        const val TRIP_NOT_COMPLETED_CODE = 40915
    }
}

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

    /**
     * `multipart/form-data` 요청. 채팅방 생성(POST /chat-rooms)만 이 형태다 —
     * `request` 파트가 `application/json` 이고 썸네일 파트는 선택이다.
     * 파일을 붙이지 않아도 서버는 `request` 파트만으로 201을 준다.
     */
    suspend fun sendMultipartForObject(
        method: String,
        path: String,
        jsonPartName: String,
        jsonPart: JSONObject
    ): JSONObject {
        val text = requestWithPayload(method, path, multipartPayload(jsonPartName, jsonPart))
        return if (text.isBlank()) JSONObject() else JSONObject(text)
    }

    /**
     * 파일 파트가 들어가는 `multipart/form-data` 요청 — 사진 공유(POST messages/images)가 이 형태다.
     * 스펙의 파트 이름은 `image`(파일) 와 `caption`(문자열)이고, 캡션은 선택이다.
     * JSON 파트가 아니라 바이너리라 [multipartPayload] 와 조립 방식이 다르다.
     */
    suspend fun sendMultipartFileForObject(
        method: String,
        path: String,
        file: MultipartFile,
        textParts: Map<String, String> = emptyMap()
    ): JSONObject {
        val text = requestWithPayload(method, path, multipartFilePayload(file, textParts))
        return if (text.isBlank()) JSONObject() else JSONObject(text)
    }

    private suspend fun request(method: String, path: String, body: JSONObject? = null): String =
        requestWithPayload(method, path, body?.let(::jsonPayload))

    private suspend fun requestWithPayload(method: String, path: String, payload: RequestPayload?): String {
        val token = accessToken()
        return try {
            execute(method, path, payload, token)
        } catch (error: MoyeoApiException) {
            if (error.statusCode != 401) throw error
            val refreshed = refreshAccessToken() ?: throw error
            execute(method, path, payload, refreshed)
        }
    }

    private suspend fun execute(method: String, path: String, payload: RequestPayload?, token: String?): String =
        withContext(Dispatchers.IO) {
            val connection = connectionFactory(URL("$rootUrl$path"))
            try {
                connection.requestMethod = method
                connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
                connection.readTimeout = READ_TIMEOUT_MILLIS
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty(
                    "Content-Type",
                    payload?.contentType ?: "application/json; charset=utf-8"
                )
                if (!token.isNullOrBlank()) {
                    connection.setRequestProperty("Authorization", "Bearer $token")
                }
                if (payload != null) {
                    connection.doOutput = true
                    connection.outputStream.use { stream -> stream.write(payload.bytes) }
                }

                val statusCode = connection.responseCode
                val responseText = if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    ""
                } else {
                    connection.responseStream(statusCode)?.readTextSafely().orEmpty()
                }
                if (statusCode !in 200..299) {
                    throw MoyeoApiException(
                        statusCode,
                        responseText.apiErrorMessage(statusCode),
                        responseText.apiErrorCode()
                    )
                }
                responseText
            } finally {
                connection.disconnect()
            }
        }

    private class RequestPayload(val contentType: String, val bytes: ByteArray)

    private fun jsonPayload(body: JSONObject) = RequestPayload(
        contentType = "application/json; charset=utf-8",
        bytes = body.toString().toByteArray(Charsets.UTF_8)
    )

    private fun multipartPayload(partName: String, part: JSONObject): RequestPayload {
        val boundary = "moyeo-${System.nanoTime()}"
        val body = buildString {
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"$partName\"\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n\r\n")
            append(part.toString())
            append("\r\n--$boundary--\r\n")
        }
        return RequestPayload(
            contentType = "multipart/form-data; boundary=$boundary",
            bytes = body.toByteArray(Charsets.UTF_8)
        )
    }

    private fun multipartFilePayload(file: MultipartFile, textParts: Map<String, String>): RequestPayload {
        val boundary = "moyeo-${System.nanoTime()}"
        val prologue = buildString {
            textParts.forEach { (name, value) ->
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"$name\"\r\n")
                append("Content-Type: text/plain; charset=utf-8\r\n\r\n")
                append(value)
                append("\r\n")
            }
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"${file.partName}\"; filename=\"${file.fileName}\"\r\n")
            append("Content-Type: ${file.mimeType}\r\n\r\n")
        }
        val epilogue = "\r\n--$boundary--\r\n"
        return RequestPayload(
            contentType = "multipart/form-data; boundary=$boundary",
            bytes = prologue.toByteArray(Charsets.UTF_8) + file.bytes + epilogue.toByteArray(Charsets.UTF_8)
        )
    }

    companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 8_000
        private const val READ_TIMEOUT_MILLIS = 15_000
    }
}

/** multipart 파일 파트 한 건. [partName] 은 스펙의 파트 이름(사진 공유는 `image`)이다. */
data class MultipartFile(val partName: String, val fileName: String, val mimeType: String, val bytes: ByteArray) {
    // ByteArray 는 equals/hashCode 가 참조 비교라 data class 기본 구현을 쓰면 값 비교가 깨진다.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipartFile) return false
        return partName == other.partName &&
            fileName == other.fileName &&
            mimeType == other.mimeType &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int {
        var result = partName.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
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

private fun String.apiErrorCode(): Int? = runCatching { JSONObject(this) }.getOrNull()?.intOrNull("code")

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
