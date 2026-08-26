package kr.hanchae.moyeotrip.data.tourism

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

class TourismContentApiException(val statusCode: Int, message: String) : Exception(message)

class HttpTourismContentRepository(
    baseUrl: String,
    private val accessToken: () -> String?,
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
                val message = runCatching { JSONObject(text).optString("errorMessage") }.getOrNull()
                throw TourismContentApiException(status, message?.takeIf(String::isNotBlank) ?: "여행지 요청 실패 ($status)")
            }
            text
        } finally {
            connection.disconnect()
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}

class FallbackTourismContentRepository(
    private val primary: TourismContentRepository,
    private val fallback: TourismContentRepository
) : TourismContentRepository {
    override suspend fun contents(keyword: String?, contentTypeId: Int?, page: Int, size: Int): TourismContentPage =
        runCatching { primary.contents(keyword, contentTypeId, page, size) }
            .getOrElse { fallback.contents(keyword, contentTypeId, page, size) }

    override suspend fun content(contentId: String): TourismContentDetail = runCatching { primary.content(contentId) }
        .getOrElse { fallback.content(contentId) }

    override suspend fun types(): List<TourismContentTypeOption> = runCatching { primary.types() }
        .getOrElse { fallback.types() }
}

object SampleTourismContentRepository : TourismContentRepository {
    private val details = listOf(
        sample("2864117", 12, "주왕산국립공원", "경상북도 청송군 부동면 공원길 226", 36.3931, 129.1728),
        sample("2871004", 12, "주산지", "경상북도 청송군 부동면 주산지길 259", 36.3494, 129.1436),
        sample(
            "2299341",
            39,
            "달기약수터 백숙거리",
            "경상북도 청송군 청송읍 약수길 5",
            36.427812,
            129.048915,
            zipcode = "37411",
            telephone = "054-873-7777",
            telephoneName = "달기약수터 관리사무소",
            homepage = "https://www.cheongsong.go.kr/tour",
            overview = "탄산이 섞인 달기약수로 끓여내는 백숙이 유명한 거리예요. 산행 뒤 늦은 점심 자리로 많이 찾아요.",
            menuNames = listOf("닭백숙 정식", "오리 백숙", "한방 삼계탕", "더덕구이")
        ),
        sample("2740882", 32, "청송 솔기온천 한옥스테이", "경상북도 청송군 청송읍 금월로 273", 36.4361, 129.0573),
        sample("2510773", 12, "청송 객주문학관", "경상북도 청송군 진보면 청송로 6359", 36.4739, 129.0093)
    )

    override suspend fun contents(keyword: String?, contentTypeId: Int?, page: Int, size: Int): TourismContentPage {
        // 서버 `keyword` 와 같은 규칙으로 거른다(제목·기본주소·상세주소, 앞뒤 공백 제거).
        val term = keyword?.trim().orEmpty()
        val filtered = details.map(TourismContentDetail::summary).filter { summary ->
            (contentTypeId == null || summary.contentTypeId == contentTypeId) &&
                (
                    term.isEmpty() ||
                        summary.title.contains(term, true) ||
                        listOfNotNull(summary.address1, summary.address2).any { it.contains(term, true) }
                    )
        }
        return TourismContentPage(
            filtered,
            page = 0,
            size = filtered.size,
            totalElements = filtered.size.toLong(),
            totalPages = 1
        )
    }

    override suspend fun content(contentId: String): TourismContentDetail =
        details.firstOrNull { it.summary.contentId == contentId } ?: details[2]

    /** 캡처·미로그인용 후보 — 목데이터 방문지가 쓰는 세 타입만 둔다(화면기획 17-1a 칩과 같은 순서). */
    override suspend fun types(): List<TourismContentTypeOption> = listOf(
        TourismContentTypeOption(12, "관광지"),
        TourismContentTypeOption(39, "식당"),
        TourismContentTypeOption(32, "숙박")
    )

    private fun sample(
        contentId: String,
        contentTypeId: Int,
        title: String,
        address: String,
        latitude: Double,
        longitude: Double,
        zipcode: String? = null,
        telephone: String? = null,
        telephoneName: String? = null,
        homepage: String? = null,
        overview: String? = null,
        menuNames: List<String> = emptyList()
    ) = TourismContentDetail(
        summary = TourismContentSummary(
            contentId = contentId,
            contentTypeId = contentTypeId,
            title = title,
            address1 = address,
            address2 = null,
            thumbnailUrl = null,
            longitude = longitude,
            latitude = latitude
        ),
        zipcode = zipcode,
        telephone = telephone,
        telephoneName = telephoneName,
        homepage = homepage,
        bookTour = null,
        overview = overview,
        contentImageUrls = emptyList(),
        menuImageUrls = emptyList(),
        menuNames = menuNames
    )
}

private fun JSONObject.toSummary() = TourismContentSummary(
    contentId = getLong("contentId").toString(),
    contentTypeId = getInt("contentTypeId"),
    title = getString("title"),
    address1 = stringOrNull("address1"),
    address2 = stringOrNull("address2"),
    thumbnailUrl = stringOrNull("thumbnail"),
    longitude = doubleOrNull("longitude"),
    latitude = doubleOrNull("latitude")
)

private val ANCHOR_HREF = Regex("""href\s*=\s*["']([^"']*)["']""", RegexOption.IGNORE_CASE)
private val HTML_TAG = Regex("<[^>]*>")

/**
 * `homepage` 정리 — 서버는 `<a href="…" target="_blank" …>표시 텍스트</a>` 를 그대로 준다.
 * href 를 우선 쓰고, 없으면 태그를 벗긴 표시 텍스트를 쓴다. 둘 다 없으면 null 이라 화면에서 줄이 사라진다.
 */
internal fun tourismHomepageValue(raw: String?): String? {
    val value = raw?.trim()?.takeIf(String::isNotEmpty) ?: return null
    ANCHOR_HREF.find(value)
        ?.groupValues
        ?.get(1)
        ?.unescapeHtml()
        ?.takeIf(String::isNotEmpty)
        ?.let { return it }
    return HTML_TAG.replace(value, " ").unescapeHtml().takeIf(String::isNotEmpty)
}

private fun String.unescapeHtml(): String = replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace(Regex("\\s+"), " ")
    .trim()

private fun JSONObject.stringOrNull(key: String): String? =
    if (!has(key) || isNull(key)) null else optString(key).takeIf(String::isNotBlank)

private fun JSONObject.doubleOrNull(key: String): Double? =
    if (!has(key) || isNull(key)) null else optDouble(key).takeUnless(Double::isNaN)

/** `TourismContentImageResponse` — 서버가 주는 이미지 키는 `originalImageUrl` 하나다. */
private fun JSONArray?.imageUrls(): List<String> = this
    .objectsOrEmpty { value -> value.stringOrNull("originalImageUrl") }
    .filterNotNull()
    .distinct()

private fun <T> JSONArray.objects(transform: (JSONObject) -> T): List<T> =
    List(length()) { index -> transform(getJSONObject(index)) }

private fun <T> JSONArray?.objectsOrEmpty(transform: (JSONObject) -> T): List<T?> =
    if (this == null) emptyList() else List(length()) { index -> optJSONObject(index)?.let(transform) }

private fun HttpURLConnection.responseStream(statusCode: Int): InputStream? =
    if (statusCode in 200..299) inputStream else errorStream
