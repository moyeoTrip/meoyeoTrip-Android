package kr.hanchae.moyeotrip.data.tourism

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TourismContentRepositoryTest {
    @Test
    fun listUsesProtectedEndpointAndParsesSummaryContract() = runBlocking {
        val connection = TourismJsonConnection(
            URL("https://example.test"),
            """{
              "items":[{
                "contentId":2299341,"contentTypeId":39,"title":"달기약수터 백숙거리",
                "address1":"경상북도 청송군","address2":null,
                "firstImageUrl":"https://cdn.test/full.jpg","firstThumbnailUrl":"https://cdn.test/thumb.jpg",
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
        assertEquals(129.048915, page.items.single().longitude!!, 0.000001)
        assertEquals(1L, page.totalElements)
    }

    @Test
    fun detailParsesFieldsThatAreAbsentFromListContract() = runBlocking {
        val connection = TourismJsonConnection(
            URL("https://example.test"),
            """{
              "contentId":2299341,"contentTypeId":39,"title":"달기약수터 백숙거리",
              "address1":"경상북도 청송군","address2":"청송읍","zipcode":"37411",
              "telephone":"054-873-7777","telephoneName":"관리사무소",
              "homepage":"https://example.test/place","bookTour":"Y","overview":"백숙이 유명한 거리예요.",
              "firstImageUrl":null,"firstThumbnailUrl":null,"longitude":129.0,"latitude":36.4,
              "introDetails":[{"restdatefood":"매주 월요일"}],
              "additionalDetails":[{"menu":"닭백숙 정식"},{"infoname":"오리 백숙"}],
              "contentImages":[{"originimgurl":"https://cdn.test/place-1.jpg"}],
              "menuImages":[{"smallimageurl":"https://cdn.test/menu-1.jpg"}]
            }"""
        )
        val repository = HttpTourismContentRepository("https://example.test", { "token" }) { connection }

        val detail = repository.content("2299341")

        assertEquals("Y", detail.bookTour)
        assertEquals("관리사무소", detail.telephoneName)
        assertEquals(listOf("https://cdn.test/place-1.jpg"), detail.contentImageUrls)
        assertEquals(listOf("https://cdn.test/menu-1.jpg"), detail.menuImageUrls)
        assertEquals(listOf("닭백숙 정식", "오리 백숙"), detail.menuNames)
    }

    @Test
    fun unauthorizedOrUnavailableApiFallsBackToDeterministicSample() = runBlocking {
        val unauthorized = object : TourismContentRepository {
            override suspend fun contents(contentTypeId: Int?, page: Int, size: Int): TourismContentPage =
                throw TourismContentApiException(401, "Unauthorized")

            override suspend fun content(contentId: String): TourismContentDetail =
                throw TourismContentApiException(401, "Unauthorized")
        }
        val repository = FallbackTourismContentRepository(unauthorized, SampleTourismContentRepository)

        assertTrue(repository.contents(null).items.isNotEmpty())
        assertEquals("달기약수터 백숙거리", repository.content("2299341").summary.title)
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
