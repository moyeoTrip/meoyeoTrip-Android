package kr.hanchae.moyeotrip.data.tourism

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.hanchae.moyeotrip.domain.auth.SignupGateStage
import org.json.JSONArray
import org.json.JSONObject

data class TourismContentSummary(
    val contentId: String,
    val contentTypeId: Int,
    val title: String,
    val address1: String?,
    val address2: String?,
    /** 서버 `thumbnail` — 목록·상세가 같은 키를 쓴다. 좌표처럼 없으면 null 이다. */
    val thumbnailUrl: String?,
    val longitude: Double?,
    val latitude: Double?
)

data class TourismContentDetail(
    val summary: TourismContentSummary,
    val zipcode: String?,
    val telephone: String?,
    val telephoneName: String?,
    val homepage: String?,
    val bookTour: String?,
    val overview: String?,
    val contentImageUrls: List<String>,
    val menuImageUrls: List<String>,
    val menuNames: List<String>
)

data class TourismContentPage(
    val items: List<TourismContentSummary>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

/** GET tourism-contents/types 의 한 항목 — 17-1a 타입 필터 칩의 후보다. */
data class TourismContentTypeOption(val contentTypeId: Int, val contentTypeName: String)

interface TourismContentRepository {
    /**
     * 17-1a 방문지 검색. [keyword] 는 서버가 제목·기본주소·상세주소로 매칭한다 —
     * 클라이언트에서 다시 거르지 않는다. 비어 있으면 파라미터를 보내지 않고 전체를 조회한다.
     */
    suspend fun contents(
        keyword: String? = null,
        contentTypeId: Int? = null,
        page: Int = 0,
        size: Int = 100
    ): TourismContentPage

    suspend fun content(contentId: String): TourismContentDetail

    suspend fun types(): List<TourismContentTypeOption>
}

class TourismContentApiException(val statusCode: Int, message: String, val errorCode: Int? = null) :
    Exception(message) {
    /** 가입이 안 끝나 서버가 막은 것(409 40902·40918) — 여행지 조회 실패가 아니다(정본 R1). */
    val signupGate: SignupGateStage? get() = SignupGateStage.ofCode(errorCode)
}

/**
 * 방문지 검색은 TourAPI 프록시이지만 **보호 API 라 가입 완료 검사를 받는다**.
 * 그래서 여기서도 가입 게이트를 알아채야 한다 — 못 알아채면 "여행지 요청 실패"라는
 * 엉뚱한 오류만 뜨고 사용자는 왜 막혔는지 알 수 없다.
 */
class HttpTourismContentRepository(
    baseUrl: String,
    private val accessToken: () -> String?,
    private val onSignupGate: (SignupGateStage) -> Unit = {},
    private val connectionFactory: (URL) -> HttpURLConnection = { it.openConnection() as HttpURLConnection }
) : TourismContentRepository {
    private val rootUrl = baseUrl.trimEnd('/')

    override suspend fun contents(keyword: String?, contentTypeId: Int?, page: Int, size: Int): TourismContentPage {
        val parameters = buildList {
            keyword?.trim()?.takeIf(String::isNotEmpty)?.let { add("keyword=${encode(it)}") }
            contentTypeId?.let { add("contentTypeId=${encode(it.toString())}") }
            add("page=${page.coerceAtLeast(0)}")
            add("size=${size.coerceIn(1, 100)}")
        }.joinToString("&")
        val json = request("/api/v1/tourism-contents?$parameters")
        return TourismContentPage(
            items = json.getJSONArray("items").objects(JSONObject::toSummary),
            page = json.optInt("page", page),
            size = json.optInt("size", size),
            totalElements = json.optLong("totalElements"),
            totalPages = json.optInt("totalPages")
        )
    }

    override suspend fun content(contentId: String): TourismContentDetail {
        val json = request("/api/v1/tourism-contents/${encode(contentId)}")
        return TourismContentDetail(
            summary = json.toSummary(),
            zipcode = json.stringOrNull("zipcode"),
            telephone = json.stringOrNull("telephone"),
            telephoneName = json.stringOrNull("telephoneName"),
            // `homepage` 는 앵커 태그가 그대로 오는 경우가 있다 — URL(없으면 표시 텍스트)만 남긴다.
            homepage = tourismHomepageValue(json.stringOrNull("homepage")),
            bookTour = json.stringOrNull("bookTour"),
            overview = json.stringOrNull("overview"),
            contentImageUrls = json.optJSONArray("contentImages").imageUrls(),
            menuImageUrls = json.optJSONArray("menuImages").imageUrls(),
            // 서버 상세에는 메뉴 "이름" 이 없다 — 음식점 메뉴판은 menuImages 로만 온다.
            menuNames = emptyList()
        )
    }

    override suspend fun types(): List<TourismContentTypeOption> =
        JSONArray(requestText("/api/v1/tourism-contents/types")).objects { type ->
            TourismContentTypeOption(
                contentTypeId = type.getInt("contentTypeId"),
                contentTypeName = type.optString("contentTypeName")
            )
        }

    private suspend fun request(path: String): JSONObject = JSONObject(requestText(path))

    private suspend fun requestText(path: String): String = withContext(Dispatchers.IO) {
        val connection = connectionFactory(URL("$rootUrl$path"))
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 20_000
            connection.setRequestProperty("Accept", "application/json")
            accessToken()?.takeIf(String::isNotBlank)?.let {
                connection.setRequestProperty("Authorization", "Bearer $it")
            }
            val status = connection.responseCode
            val text = connection.responseStream(status)?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val body = runCatching { JSONObject(text) }.getOrNull()
                val message = body?.optString("errorMessage")
                val errorCode = body?.takeIf { it.has("code") && !it.isNull("code") }?.optInt("code")
                val failure = TourismContentApiException(
                    status,
                    message?.takeIf(String::isNotBlank) ?: "여행지 요청 실패 ($status)",
                    errorCode
                )
                failure.signupGate?.let(onSignupGate)
                throw failure
            }
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}

private fun JSONObject.toSummary() = TourismContentSummary(
    contentId = optLong("contentId").takeIf { it != 0L }?.toString() ?: optString("contentId"),
    contentTypeId = optInt("contentTypeId"),
    title = optString("title"),
    address1 = stringOrNull("address1"),
    address2 = stringOrNull("address2"),
    thumbnailUrl = stringOrNull("thumbnail"),
    longitude = doubleOrNull("longitude"),
    latitude = doubleOrNull("latitude")
)

/**
 * `contentImages` · `menuImages` 는 `{contentId, originalImageUrl}` 객체 배열이다.
 * URL 이 비어 있는 항목은 화면에 그릴 수 없으므로 버린다.
 */
private fun JSONArray?.imageUrls(): List<String> = this
    ?.objects { image -> image.stringOrNull("originalImageUrl") }
    ?.filterNotNull()
    .orEmpty()

private fun <T> JSONArray.objects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

private fun JSONObject.stringOrNull(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeIf(String::isNotBlank)

private fun JSONObject.doubleOrNull(key: String): Double? =
    if (!has(key) || isNull(key)) null else optDouble(key).takeUnless(Double::isNaN)

private fun HttpURLConnection.responseStream(statusCode: Int): InputStream? =
    if (statusCode in 200..299) inputStream else errorStream

/**
 * TourAPI 의 `homepage` 는 앵커 태그가 통째로 오는 경우가 있다
 * (`<a href="http://..." target="_blank">http://...</a>`). 화면에 태그가 그대로 나가지 않게
 * href(없으면 표시 텍스트)만 남긴다. 남길 게 없으면 null 이라 화면이 그 줄을 숨긴다.
 */
internal fun tourismHomepageValue(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (!value.contains('<')) return value.unescapeHtmlAmpersand()
    val href = Regex("""href\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
        .find(value)
        ?.groupValues
        ?.get(1)
        ?.trim()
        ?.takeIf(String::isNotEmpty)
    if (href != null) return href.unescapeHtmlAmpersand()
    return value.replace(Regex("<[^>]*>"), "")
        .trim()
        .takeIf(String::isNotEmpty)
        ?.unescapeHtmlAmpersand()
}

private fun String.unescapeHtmlAmpersand(): String = replace("&amp;", "&")
