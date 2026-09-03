package kr.hanchae.moyeotrip.data.rooms

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.MoyeoApiException
import kr.hanchae.moyeotrip.data.api.MultipartFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ChatRoomRepositoryTest {
    @Test
    fun searchParsesServerRoomsAndSendsBearerToken() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/search"),
            """
            [{"roomId":1,"title":"안동 역사 여행","description":"안동을 함께 여행해요.","thumbnail":null,
              "tripType":"DAY_TRIP","startDate":"2026-09-12","endDate":null,
              "recruitmentDeadlineDate":"2026-09-09","hostId":41,"participantCount":1,"maxParticipants":5,
              "courseTitle":"안동 역사·야경 코스","tags":[{"tagId":1,"name":"섬"},{"tagId":4,"name":"자연"}]}]
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val rooms = repository.search()

        assertEquals(1, rooms.size)
        assertEquals("안동 역사 여행", rooms.first().title)
        assertEquals(listOf("섬", "자연"), rooms.first().tags.map(RoomTag::name))
        assertNull(rooms.first().thumbnail)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
    }

    /** 2026-08-24 서버 패치로 목록 응답에 생긴 9필드 — 카드 배지·찜·지도 마커의 근거다. */
    @Test
    fun searchParsesFieldsAddedByServerPatch() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/search"),
            """
            [{"roomId":1,"title":"안동 역사 여행","description":null,"thumbnail":null,
              "tripType":"DAY_TRIP","startDate":"2026-09-12","endDate":null,
              "dayTripStartTime":"09:00:00","dayTripEndTime":"18:00:00",
              "recruitmentDeadlineDate":"2026-09-09","recruitmentDDay":15,"status":"RECRUITING",
              "favorite":true,"meetingLatitude":36.576,"meetingLongitude":128.97,
              "meetingDetails":"안동역 1번 출구 앞","meetingDateTime":"2026-09-12T08:30:00","hostId":41,
              "participantCount":1,"maxParticipants":5,"courseTitle":"안동 역사·야경 코스","tags":[]}]
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val room = repository.search().single()

        assertEquals(true, room.favorite)
        assertEquals(15, room.recruitmentDDay)
        assertEquals("RECRUITING", room.status)
        assertEquals(36.576, room.meetingLatitude!!, 0.0001)
        assertEquals(128.97, room.meetingLongitude!!, 0.0001)
        assertEquals("안동역 1번 출구 앞", room.meetingDetails)
        assertEquals("2026-09-12T08:30:00", room.meetingDateTime)
        assertEquals("09:00:00", room.dayTripStartTime)
        assertEquals("18:00:00", room.dayTripEndTime)
    }

    /** 숙박 방·집합 미정은 서버가 null 을 준다 — 파서가 값을 만들어내면 안 된다. */
    @Test
    fun searchKeepsNullsForOvernightAndUndecidedMeetingPoint() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/search"),
            """
            [{"roomId":2,"title":"울릉도 2박 3일","description":null,"thumbnail":null,
              "tripType":"OVERNIGHT","startDate":"2026-09-12","endDate":"2026-09-14",
              "dayTripStartTime":null,"dayTripEndTime":null,
              "recruitmentDeadlineDate":"2026-09-09","recruitmentDDay":0,"status":"CONFIRMED",
              "favorite":false,"meetingLatitude":null,"meetingLongitude":null,
              "meetingDetails":null,"meetingDateTime":"2026-09-12T08:30:00","hostId":41,
              "participantCount":3,"maxParticipants":5,"courseTitle":"울릉도 코스","tags":[]}]
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val room = repository.search().single()

        assertNull(room.dayTripStartTime)
        assertNull(room.dayTripEndTime)
        assertNull(room.meetingLatitude)
        assertNull(room.meetingLongitude)
        assertNull(room.meetingDetails)
        assertEquals("2026-09-14", room.endDate)
        assertEquals(0, room.recruitmentDDay)
        assertEquals(false, room.favorite)
    }

    @Test
    fun createRoomSendsJsonAndThumbnailPartsAsMultipartAndReadsRoomId() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms"),
            """{"roomId":41}"""
        )
        val repository = HttpChatRoomRepository(client(connection))

        // 2026-08-26 서버 변경: thumbnail 파트가 필수다(없으면 400 40041).
        val thumbnail = MultipartFile(
            partName = "thumbnail",
            fileName = "placeholder-landscape.webp",
            mimeType = "image/webp",
            bytes = byteArrayOf(0x52, 0x49, 0x46, 0x46)
        )
        val roomId = repository.createRoom(overnightRoom(), thumbnail)

        assertEquals(41L, roomId)
        assertEquals("POST", connection.requestMethod)
        val contentType = connection.getRequestProperty("Content-Type").orEmpty()
        assertTrue(contentType.startsWith("multipart/form-data; boundary="))
        val body = connection.requestBody.toString(Charsets.UTF_8.name())
        assertTrue(body.contains("name=\"request\""))
        assertTrue(body.contains("Content-Type: application/json; charset=utf-8"))
        assertTrue(body.contains("\"courseType\":\"PUBLIC\""))
        // 썸네일 파트가 실제로 붙는지 — 파트 이름·파일명·MIME 까지 본다
        assertTrue(body.contains("name=\"thumbnail\"; filename=\"placeholder-landscape.webp\""))
        assertTrue(body.contains("Content-Type: image/webp"))
    }

    @Test
    fun applySendsMessageBodyAndParsesResult() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/1/applications"),
            """{"roomId":1,"result":"PENDING_APPROVAL"}"""
        )
        val repository = HttpChatRoomRepository(client(connection))

        val result = repository.apply(1, "잘 부탁드려요. 함께 여행하고 싶어요!")

        assertEquals(RoomApplicationResult.PENDING_APPROVAL, result)
        assertEquals("POST", connection.requestMethod)
        assertTrue(connection.requestBody.toString(Charsets.UTF_8.name()).contains("applicationMessage"))
    }

    @Test
    fun cancelApplicationAcceptsNoContent() = runBlocking {
        val connection = NoContentConnection(URL("https://example.test/api/v1/chat-rooms/1/applications/me"))
        val repository = HttpChatRoomRepository(client(connection))

        repository.cancelApplication(1)

        assertEquals("DELETE", connection.requestMethod)
    }

    @Test
    fun membersParsesRoleFlagsAndCounts() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/21/members"),
            """
            {"participantCount":2,"maxParticipants":4,"waitlistCount":0,"members":[
              {"userId":62,"nickname":"따스한 기린 2334","profileImageUrl":null,"completedTripCount":0,
               "host":true,"me":false},
              {"userId":61,"nickname":"즐거운 고양이 4760","profileImageUrl":"https://cdn.test/a.webp",
               "completedTripCount":3,"host":false,"me":true}]}
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val members = repository.members(21)

        assertEquals(4, members.maxParticipants)
        assertEquals(0, members.waitlistCount)
        assertTrue(members.members.first().host)
        assertEquals(61L, members.members.single(RoomMember::me).userId)
        assertEquals(3, members.members.last().completedTripCount)
    }

    @Test
    fun noticesSplitsPinnedAndUnpinned() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/21/notices"),
            """
            {"pinnedNotices":[{"noticeId":21,"content":"집합 안내","pinned":true,
              "authorNickname":"따스한 기린 2334","createdAt":"2026-08-24T01:12:20.560012"}],
             "unpinnedNotices":[{"noticeId":41,"content":"재검증 공지","pinned":false,
              "authorNickname":"따스한 기린 2334","createdAt":"2026-08-24T08:04:37.218795"}]}
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val notices = repository.notices(21)

        assertEquals(listOf(21L), notices.pinned.map(RoomNotice::noticeId))
        assertEquals(listOf(41L), notices.unpinned.map(RoomNotice::noticeId))
        assertEquals(2, notices.all.size)
    }

    @Test
    fun currentRoadmapKeepsNullDayAndSortsPlaces() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/21/roadmap/current"),
            """
            {"active":false,"dayNumber":null,"totalDays":1,"currentPlace":null,"nextPlace":null,
             "places":[{"contentId":2,"sequence":2,"title":"주산지","thumbnail":null,"latitude":36.3,
               "longitude":129.1,"scheduledAt":null,"progress":"UPCOMING"},
              {"contentId":1,"sequence":1,"title":"주왕산","thumbnail":null,"latitude":null,
               "longitude":null,"scheduledAt":"2026-09-20T09:00:00","progress":"CURRENT"}]}
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val roadmap = repository.currentRoadmap(21)

        assertNull(roadmap.dayNumber)
        assertEquals(listOf("주왕산", "주산지"), roadmap.places.map(RoadmapPlace::title))
        assertEquals(1, roadmap.totalDays)
    }

    @Test
    fun messagesSendsPagingQueryAndParsesSenders() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/21/messages"),
            """
            {"messages":[{"messageId":21,"type":"SYSTEM","senderId":null,"senderNickname":"시스템",
               "content":"모임이 개설됐어요.","createdAt":"2026-08-23T20:11:35.942505","imageUrl":null},
              {"messageId":50,"type":"USER","senderId":62,"senderNickname":"따스한 기린 2334",
               "content":"도착했어요!","createdAt":"2026-08-24T01:19:16.185853","imageUrl":null}],
             "nextId":null,"hasNext":false}
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val page = repository.messages(21, limit = 50)

        assertEquals(2, page.messages.size)
        assertNull(page.messages.first().senderId)
        assertEquals(62L, page.messages.last().senderId)
        assertNull(page.nextId)
        assertEquals(false, page.hasNext)
    }

    @Test
    fun sendMessageAlwaysIncludesMentionedUserIds() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/chat-rooms/21/messages"),
            """
            {"messageId":74,"type":"USER","senderId":61,"senderNickname":"즐거운 고양이 4760",
             "content":"안드로이드 연동 재검증 메시지","createdAt":"2026-08-24T08:36:21.120508189","imageUrl":null}
            """.trimIndent()
        )
        val repository = HttpChatRoomRepository(client(connection))

        val sent = repository.sendMessage(21, "안드로이드 연동 재검증 메시지")

        assertEquals("POST", connection.requestMethod)
        val body = connection.requestBody.toString(Charsets.UTF_8.name())
        assertTrue(body.contains("\"mentionedUserIds\""))
        assertEquals(74L, sent.messageId)
        assertEquals(61L, sent.senderId)
    }

    @Test
    fun errorResponsesSurfaceServerMessage() {
        val connection = ErrorConnection(
            URL("https://example.test/api/v1/chat-rooms/999"),
            statusCode = 404,
            response = """{"errorMessage":"모집을 찾을 수 없어요."}"""
        )
        val repository = HttpChatRoomRepository(client(connection))

        try {
            runBlocking { repository.room(999) }
            fail("404는 MoyeoApiException 으로 떠야 합니다.")
        } catch (error: MoyeoApiException) {
            assertEquals(404, error.statusCode)
            assertEquals("모집을 찾을 수 없어요.", error.message)
        }
    }

    private fun overnightRoom() = NewChatRoom(
        title = "검증용 방",
        description = null,
        maxParticipants = 5,
        dayTrip = false,
        startDate = "2026-09-12",
        endDate = "2026-09-13",
        dayTripStartTime = null,
        dayTripEndTime = null,
        recruitmentDeadlineDate = "2026-09-09",
        meetingLatitude = 36.576,
        meetingLongitude = 128.97,
        meetingDetails = null,
        meetingDateTime = "2026-09-12T08:30:00",
        participationFee = 0,
        genderRestriction = "NONE",
        minimumAge = 25,
        maximumAge = 35,
        joinApprovalMode = "AUTO",
        publicCourseId = 21
    )

    private fun client(connection: HttpURLConnection) = MoyeoApiClient(
        baseUrl = "https://example.test",
        accessToken = { "access-token" },
        connectionFactory = { connection }
    )
}

private class JsonConnection(url: URL, response: String) : HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()
    val requestBody = ByteArrayOutputStream()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = HTTP_OK

    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBytes)

    override fun getOutputStream() = requestBody
}

private class NoContentConnection(url: URL) : HttpURLConnection(url) {
    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = HTTP_NO_CONTENT

    override fun getInputStream(): InputStream {
        fail("204 응답에서는 본문을 읽으면 안 됩니다.")
        error("unreachable")
    }
}

private class ErrorConnection(url: URL, private val statusCode: Int, response: String) : HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = statusCode

    override fun getInputStream(): InputStream = error("에러 응답에서는 errorStream 을 읽어야 합니다.")

    override fun getErrorStream(): InputStream = ByteArrayInputStream(responseBytes)
}
