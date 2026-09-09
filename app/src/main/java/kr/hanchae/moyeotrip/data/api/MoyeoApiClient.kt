package kr.hanchae.moyeotrip.data.api

import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.hanchae.moyeotrip.domain.auth.SignupGateStage
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

    /**
     * 지도 조회(`GET /chat-rooms/map`)의 위경도·반경이 유효 범위를 벗어난 것(`400 40040`).
     *
     * 서버가 2026-08-30 로 `radiusKm` 상한 200km 를 넣었다(실측: 500 → 400, 120 → 200).
     * 안드로이드는 탐색 지도가 `GYEONGBUK_RADIUS_KM = 120.0` 을 고정으로 보내 지금은 걸리지 않지만,
     * 범위를 벗어난 요청이 조용히 빈 지도로 보이면 원인을 알 수 없다 — 오류 문구로 갈라낸다.
     */
    val invalidMapSearchArea: Boolean get() = statusCode == 400 && errorCode == INVALID_MAP_SEARCH_AREA_CODE

    /**
     * 가입이 아직 안 끝나 일반 API 가 막힌 것(`40902`·`40918`).
     * 토큰 만료가 아니므로 재발급이 아니라 해당 가입 단계로 돌아가야 한다(정본 R1).
     */
    val signupGate: SignupGateStage? get() = SignupGateStage.ofCode(errorCode)

    companion object {
        const val TRIP_NOT_COMPLETED_CODE = 40915

        /** `40040 INVALID_MAP_SEARCH_AREA` — 위도·경도 범위 초과와 `radiusKm` 상한 초과가 같은 코드다. */
        const val INVALID_MAP_SEARCH_AREA_CODE = 40040
    }
}

/**
 * 보호 API 공용 HTTP 클라이언트 — HttpAuthGateway·HttpTourismContentRepository 와 같은
 * HttpURLConnection + org.json 조합을 그대로 따른다. 401이면 [refreshAccessToken] 으로
 * 한 번 재발급을 시도한 뒤 같은 요청을 다시 보낸다.
 *
 * [onSignupGate] 는 가입이 안 끝나 서버가 막은 경우([MoyeoApiException.signupGate])에 불린다.
 * 이 신호를 받은 쪽이 사용자를 해당 가입 단계로 되돌린다 — 클라이언트가 요청을 되풀이할 일이 아니다.
 */
class MoyeoApiClient(
    baseUrl: String,
    private val accessToken: () -> String?,
    private val refreshAccessToken: suspend () -> String? = { null },
    private val onSignupGate: (SignupGateStage) -> Unit = {},
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
     * `multipart/form-data` 요청 — JSON 파트만 보낸다.
     *
     * 채팅방 생성에는 더 이상 쓰지 않는다: 2026-08-26 서버 변경으로 `thumbnail` 파트가
     * **필수**가 됐다(없으면 400 `40041` "채팅방 썸네일 이미지는 필수입니다"). 실서버로 확인했다.
     * 채팅방 생성은 [sendMultipartJsonAndFileForObject] 를 쓴다.
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
            // 가입 게이트는 재발급 경로보다 **먼저** 걸러야 한다. 새 토큰을 받아도 서버는 같은 409 를 돌려주므로
            // 401 판정에 섞이는 순간 재발급 → 409 → 재발급 무한 재시도가 된다(정본 R1).
            // 상태 코드가 아니라 오류 코드로 본다 — 서버가 401 에 실어 보내도 여기서 멈춘다.
            val gate = error.signupGate
            if (gate != null) {
                onSignupGate(gate)
                throw error
            }
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

    /**
     * JSON 파트와 파일 파트를 함께 보내는 `multipart/form-data` — 채팅방 생성이 이 형태다.
     * 서버 스펙의 파트 이름은 `request`(application/json) 와 `thumbnail`(바이너리)이고 둘 다 필수다.
     */
    suspend fun sendMultipartJsonAndFileForObject(
        method: String,
        path: String,
        jsonPartName: String,
        jsonPart: JSONObject,
        file: MultipartFile
    ): JSONObject {
        val text = requestWithPayload(method, path, multipartJsonAndFilePayload(jsonPartName, jsonPart, file))
        return if (text.isBlank()) JSONObject() else JSONObject(text)
    }

    /**
     * JSON 파트 + **여러 개**의 파일 파트 — 피드 작성(POST feeds)이 이 형태다.
     * 서버 스펙의 파트 이름은 `request`(application/json)와 `images`(바이너리, 최대 10장)이고 둘 다 필수다.
     */
    suspend fun sendMultipartJsonAndFilesForObject(
        method: String,
        path: String,
        jsonPartName: String,
        jsonPart: JSONObject,
        files: List<MultipartFile>
    ): JSONObject {
        val text = requestWithPayload(method, path, multipartJsonAndFilesPayload(jsonPartName, jsonPart, files))
        return if (text.isBlank()) JSONObject() else JSONObject(text)
    }

    private fun multipartJsonAndFilesPayload(
        jsonPartName: String,
        jsonPart: JSONObject,
        files: List<MultipartFile>
    ): RequestPayload {
        val boundary = "moyeo-${System.nanoTime()}"
        var bytes = buildString {
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"$jsonPartName\"\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n\r\n")
            append(jsonPart.toString())
            append("\r\n")
        }.toByteArray(Charsets.UTF_8)
        files.forEach { file ->
            val header = buildString {
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"${file.partName}\"; filename=\"${file.fileName}\"\r\n")
                append("Content-Type: ${file.mimeType}\r\n\r\n")
            }.toByteArray(Charsets.UTF_8)
            bytes = bytes + header + file.bytes + "\r\n".toByteArray(Charsets.UTF_8)
        }
        bytes = bytes + "--$boundary--\r\n".toByteArray(Charsets.UTF_8)
        return RequestPayload(contentType = "multipart/form-data; boundary=$boundary", bytes = bytes)
    }

    /**
     * JSON 파트 + **선택** 파일 파트 — 18-6 모집 내용 수정(`PATCH /chat-rooms/{id}`)이 이 형태다.
     *
     * 썸네일을 생략하면 서버가 기존 값을 유지한다(그래서 PUT 이 아니라 PATCH 다).
     * 파일이 필수인 [sendMultipartJsonAndFileForObject] 와 달리 null 을 받는다.
     */
    suspend fun sendMultipartJsonAndOptionalFile(
        method: String,
        path: String,
        jsonPartName: String,
        jsonPart: JSONObject,
        file: MultipartFile? = null
    ): JSONObject {
        val payload = if (file == null) {
            multipartJsonPayload(jsonPartName, jsonPart)
        } else {
            multipartJsonAndFilePayload(jsonPartName, jsonPart, file)
        }
        val text = requestWithPayload(method, path, payload)
        return if (text.isBlank()) JSONObject() else JSONObject(text)
    }

    /** JSON 파트 하나만 있는 multipart. 파일 파트가 없을 때 쓴다. */
    private fun multipartJsonPayload(jsonPartName: String, jsonPart: JSONObject): RequestPayload {
        val boundary = "moyeo-${System.nanoTime()}"
        val body = buildString {
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"$jsonPartName\"\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n\r\n")
            append(jsonPart.toString())
            append("\r\n--$boundary--\r\n")
        }
        return RequestPayload(
            contentType = "multipart/form-data; boundary=$boundary",
            bytes = body.toByteArray(Charsets.UTF_8)
        )
    }

    private fun multipartJsonAndFilePayload(
        jsonPartName: String,
        jsonPart: JSONObject,
        file: MultipartFile
    ): RequestPayload {
        val boundary = "moyeo-${System.nanoTime()}"
        val prologue = buildString {
            append("--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"$jsonPartName\"\r\n")
            append("Content-Type: application/json; charset=utf-8\r\n\r\n")
            append(jsonPart.toString())
            append("\r\n--$boundary\r\n")
            append("Content-Disposition: form-data; name=\"${file.partName}\"; filename=\"${file.fileName}\"\r\n")
            append("Content-Type: ${file.mimeType}\r\n\r\n")
        }
        val epilogue = "\r\n--$boundary--\r\n"
        return RequestPayload(
            contentType = "multipart/form-data; boundary=$boundary",
            bytes = prologue.toByteArray(Charsets.UTF_8) + file.bytes + epilogue.toByteArray(Charsets.UTF_8)
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
