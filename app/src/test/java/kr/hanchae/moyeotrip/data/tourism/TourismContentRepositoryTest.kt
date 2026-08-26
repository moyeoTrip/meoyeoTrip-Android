package kr.hanchae.moyeotrip.data.tourism

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TourismContentRepositoryTest {
    @Test
    fun typesParsesServerTypeCandidates() = runBlocking {
        val connection = TourismJsonConnection(
            URL("https://example.test"),
            """[{"contentTypeId":12,"contentTypeName":"관광지"},{"contentTypeId":39,"contentTypeName":"음식점"}]"""
        )
        val repository = HttpTourismContentRepository("https://example.test", { "access-token" }) { url ->
            assertEquals("/api/v1/tourism-contents/types", url.path)
            connection
        }

        val types = repository.types()

        assertEquals(listOf(12, 39), types.map(TourismContentTypeOption::contentTypeId))
        assertEquals("음식점", types.last().contentTypeName)
    }

    @Test
    fun listUsesProtectedEndpointAndParsesSummaryContract() = runBlocking {
        val connection = TourismJsonConnection(
            URL("https://example.test"),
            """{
              "items":[{
                "contentId":2299341,"contentTypeId":39,"title":"달기약수터 백숙거리",
                "address1":"경상북도 청송군","address2":null,
                "thumbnail":"https://cdn.test/thumb.jpg",
                "longitude":129.048915,"latitude":36.427812
              }],
              "page":0,"size":20,"totalElements":1,"totalPages":1
            }"""
        )
        val repository = HttpTourismContentRepository("https://example.test", { "access-token" }) { url ->
            assertEquals("/api/v1/tourism-contents", url.path)
            assertTrue(url.query.contains("contentTypeId=39"))
            connection
        }

        val page = repository.contents(contentTypeId = 39, size = 20)

        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
        assertEquals("달기약수터 백숙거리", page.items.single().title)
        assertEquals("https://cdn.test/thumb.jpg", page.items.single().thumbnailUrl)
        assertEquals(129.048915, page.items.single().longitude!!, 0.000001)
        assertEquals(1L, page.totalElements)
    }

    /** W1 — 검색은 서버가 한다. 검색어와 타입 칩을 함께 보낼 수 있어야 한다. */
    @Test
    fun searchSendsKeywordAlongsideContentTypeAndKeepsServerTotal() = runBlocking {
        var requestedQuery: String? = null
        val repository = HttpTourismContentRepository("https://example.test", { "token" }) { url ->
            requestedQuery = url.query
            TourismJsonConnection(url, """{"items":[],"page":0,"size":20,"totalElements":21,"totalPages":2}""")
        }

        val page = repository.contents(keyword = "  주왕산  ", contentTypeId = 12, size = 20)

        val encoded = URLEncoder.encode("주왕산", "UTF-8")
        val query = requireNotNull(requestedQuery)
        assertTrue(query.contains("keyword=$encoded"))
        assertTrue(query.contains("contentTypeId=12"))
        // 결과 수 표기는 클라가 센 값이 아니라 totalElements 다.
        assertEquals(21L, page.totalElements)
        assertTrue(page.items.isEmpty())
    }

    @Test
    fun blankKeywordIsNotSentAtAll() = runBlocking {
        var requestedQuery: String? = null
        val repository = HttpTourismContentRepository("https://example.test", { "token" }) { url ->
            requestedQuery = url.query
            TourismJsonConnection(url, """{"items":[],"page":0,"size":20,"totalElements":3098,"totalPages":155}""")
        }

        repository.contents(keyword = "   ")

        assertFalse(requireNotNull(requestedQuery).contains("keyword"))
    }

    /** W2 — 상세는 목록에 없는 필드까지 서버 값 그대로 읽는다(음식점은 menuImages 포함). */
    @Test
    fun detailParsesFieldsThatAreAbsentFromListContract() = runBlocking {
        val connection = TourismJsonConnection(
            URL("https://example.test"),
            """{
              "contentId":2299341,"contentTypeId":39,"title":"달기약수터 백숙거리",
              "address1":"경상북도 청송군","address2":"청송읍","zipcode":"37411",
              "telephone":"054-873-7777","telephoneName":"관리사무소",
              "homepage":"https://example.test/place","overview":"백숙이 유명한 거리예요.",
              "thumbnail":"https://cdn.test/thumb.jpg","longitude":129.0,"latitude":36.4,
              "contentImages":[
                {"contentId":2299341,"originalImageUrl":"https://cdn.test/place-1.jpg"},
                {"contentId":2299341,"originalImageUrl":"https://cdn.test/place-2.jpg"}
              ],
              "menuImages":[{"contentId":2299341,"originalImageUrl":"https://cdn.test/menu-1.jpg"}]
            }"""
        )
        val repository = HttpTourismContentRepository("https://example.test", { "token" }) { connection }

        val detail = repository.content("2299341")

        assertEquals("37411", detail.zipcode)
        assertEquals("054-873-7777", detail.telephone)
        assertEquals("관리사무소", detail.telephoneName)
        assertEquals("백숙이 유명한 거리예요.", detail.overview)
        assertEquals("https://cdn.test/thumb.jpg", detail.summary.thumbnailUrl)
        assertEquals(
            listOf("https://cdn.test/place-1.jpg", "https://cdn.test/place-2.jpg"),
            detail.contentImageUrls
        )
        assertEquals(listOf("https://cdn.test/menu-1.jpg"), detail.menuImageUrls)
    }

    /** W2 — null 필드는 지어내지 않고 null 로 남는다(화면이 그 줄을 숨긴다). */
    @Test
    fun detailKeepsMissingFieldsNullInsteadOfInventingThem() = runBlocking {
        val connection = TourismJsonConnection(
            URL("https://example.test"),
            """{
              "contentId":2864117,"contentTypeId":12,"title":"주왕산국립공원",
              "address1":"경상북도 청송군 부동면 공원길 226","address2":null,
              "zipcode":null,"telephone":null,"telephoneName":null,"homepage":null,"overview":null,
              "thumbnail":null,"longitude":null,"latitude":null,
              "contentImages":[],"menuImages":[]
            }"""
        )
        val repository = HttpTourismContentRepository("https://example.test", { "token" }) { connection }

        val detail = repository.content("2864117")

        assertNull(detail.zipcode)
        assertNull(detail.telephone)
        assertNull(detail.telephoneName)
        assertNull(detail.homepage)
        assertNull(detail.overview)
        assertNull(detail.summary.thumbnailUrl)
        assertNull(detail.summary.latitude)
        assertTrue(detail.contentImageUrls.isEmpty())
        assertTrue(detail.menuImageUrls.isEmpty())
    }

    /** W2 — `homepage` 는 앵커 태그째 온다. 태그가 화면에 나가면 안 된다. */
    @Test
    fun homepageAnchorTagIsReducedToItsUrl() {
        assertEquals(
            "http://www.141minihotel.com/",
            tourismHomepageValue(
                """<a href="http://www.141minihotel.com/" target="_blank" title="새창 : 141미니호텔">""" +
                    "http://www.141minihotel.com</a>"
            )
        )
        // href 가 비어 있으면 표시 텍스트를 쓴다.
        assertEquals("청송군 문화관광", tourismHomepageValue("""<a href="">청송군 문화관광</a>"""))
        // 이미 평문이면 그대로 둔다.
        assertEquals("https://www.cheongsong.go.kr/tour", tourismHomepageValue("https://www.cheongsong.go.kr/tour"))
        // 쿼리스트링의 &amp; 는 되돌린다.
        assertEquals("http://a.test/?x=1&y=2", tourismHomepageValue("""<a href="http://a.test/?x=1&amp;y=2">a</a>"""))
        // 남길 게 없으면 null — 화면은 그 줄을 숨긴다.
        assertNull(tourismHomepageValue("<a href=\"\"></a>"))
        assertNull(tourismHomepageValue("   "))
        assertNull(tourismHomepageValue(null))
    }

    @Test
    fun sampleRepositoryMatchesKeywordOnTitleAndAddressLikeTheServer() = runBlocking {
        val byTitle = SampleTourismContentRepository.contents(keyword = "주왕산")
        val byAddress = SampleTourismContentRepository.contents(keyword = " 청송 ")
        val withType = SampleTourismContentRepository.contents(keyword = "청송", contentTypeId = 39)

        assertEquals(listOf("주왕산국립공원"), byTitle.items.map(TourismContentSummary::title))
        assertEquals(1L, byTitle.totalElements)
        assertEquals(5, byAddress.items.size)
        assertEquals(listOf("달기약수터 백숙거리"), withType.items.map(TourismContentSummary::title))
        assertTrue(SampleTourismContentRepository.contents(keyword = "없는 장소").items.isEmpty())
    }

    @Test
    fun unauthorizedOrUnavailableApiFallsBackToDeterministicSample() = runBlocking {
        val unauthorized = object : TourismContentRepository {
            override suspend fun contents(
                keyword: String?,
                contentTypeId: Int?,
                page: Int,
                size: Int
            ): TourismContentPage = throw TourismContentApiException(401, "Unauthorized")

            override suspend fun content(contentId: String): TourismContentDetail =
                throw TourismContentApiException(401, "Unauthorized")

            override suspend fun types(): List<TourismContentTypeOption> =
                throw TourismContentApiException(401, "Unauthorized")
        }
        val repository = FallbackTourismContentRepository(unauthorized, SampleTourismContentRepository)

        assertTrue(repository.contents().items.isNotEmpty())
        assertEquals("달기약수터 백숙거리", repository.content("2299341").summary.title)
        assertEquals(listOf("관광지", "식당", "숙박"), repository.types().map(TourismContentTypeOption::contentTypeName))
    }
}

private class TourismJsonConnection(url: URL, response: String, private val status: Int = HTTP_OK) :
    HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = status

    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBytes)

    override fun getErrorStream(): InputStream = ByteArrayInputStream(responseBytes)
}
