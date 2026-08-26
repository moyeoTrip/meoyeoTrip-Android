package kr.hanchae.moyeotrip.data.courses

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 코스 상세(화면기획 14)의 "○○ 님이 다녀온 코스" 행 근거가 되는 필드들.
 * 라이브 캡처에서 이 행이 통째로 비어 있었다 — 서버가 주는 값을 파서가 버리지 않는지 고정한다.
 */
class TravelCourseRepositoryTest {
    @Test
    fun courseParsesPublisherFieldsUsedByThePlanningRow() = runBlocking {
        val repository = HttpTravelCourseRepository(
            client(
                """
                {"courseId":21,"title":"주왕산 & 주산지 힐링 트레킹!","description":"가을 단풍과 호수",
                 "creatorNickname":"따스한 기린 2334","creatorTravelStartDate":"2026-07-20",
                 "creatorTravelEndDate":null,"chatRoomCount":1,"travelTime":"9시간","distanceKm":90.7,
                 "averageRating":5.0,"ratingCount":1,"tags":[{"tagId":4,"name":"자연"}],"thumbnail":null,
                 "places":[{"contentId":2599344,"dayNumber":1,"sequence":1,"visitTime":"10:00:00",
                   "title":"(재)행복전통마을","thumbnail":null,"latitude":36.57,"longitude":128.76}]}
                """.trimIndent()
            )
        )

        val course = repository.course(21)

        assertEquals("따스한 기린 2334", course.creatorNickname)
        assertEquals("2026-07-20", course.creatorTravelStartDate)
        assertEquals(1, course.chatRoomCount)
    }

    /** 서버가 주지 않는 항목은 null 로 남는다 — 자리표시자를 지어내지 않는다. */
    @Test
    fun missingPublisherFieldsStayNull() = runBlocking {
        val repository = HttpTravelCourseRepository(
            client(
                """
                {"courseId":61,"title":"27-3 검증 코스","description":null,"travelTime":"9시간",
                 "distanceKm":90.7,"averageRating":null,"ratingCount":0,"tags":[],"thumbnail":null,
                 "places":[]}
                """.trimIndent()
            )
        )

        val course = repository.course(61)

        assertNull(course.creatorNickname)
        assertNull(course.creatorTravelStartDate)
        assertNull(course.chatRoomCount)
    }

    private fun client(response: String) = MoyeoApiClient(
        baseUrl = "https://example.test",
        accessToken = { "access-token" },
        connectionFactory = { CourseJsonConnection(it, response) }
    )
}

private class CourseJsonConnection(url: URL, response: String) : HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = HTTP_OK

    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBytes)
}
