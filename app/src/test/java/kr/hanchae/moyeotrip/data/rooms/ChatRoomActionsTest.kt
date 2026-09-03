package kr.hanchae.moyeotrip.data.rooms

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.MoyeoApiException
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * 2026-08-25 연동분 — 20-2 첨부 5종 · 20 투표 · 20-3 공지 고정 · 17-3 집합 정보 · 18 상태·승인 ·
 * 20-1 나가기·내보내기 · 27 동행자. 경로·본문·파싱이 스펙과 같은지만 본다(화면은 별도).
 */
class ChatRoomActionsTest {
    @Test
    fun shareMeetingLocationPostsWithoutBody() = runBlocking {
        val call = RecordingCall(cardMessage(type = "LOCATION"))
        val repository = HttpChatRoomRepository(call.client())

        val message = repository.shareMeetingLocation(21)

        assertEquals("POST", call.method)
        assertEquals("/api/v1/chat-rooms/21/messages/locations", call.path)
        // 스펙에 요청 본문이 없다 — 빈 JSON도 보내지 않는다
        assertEquals("", call.body())
        assertEquals("LOCATION", message.type)
        assertEquals("안동역 1번 출구 앞", message.location?.name)
        assertEquals(36.576, message.location!!.latitude, 0.0001)
    }

    @Test
    fun shareTourismContentAndSettlementMemoSendSpecFieldNames() = runBlocking {
        val place = RecordingCall(cardMessage(type = "TOURISM_CONTENT"))
        HttpChatRoomRepository(place.client()).shareTourismContent(21, 126508)
        assertEquals("/api/v1/chat-rooms/21/messages/tourism-contents", place.path)
        assertEquals(126508L, JSONObject(place.body()).getLong("contentId"))

        val memo = RecordingCall(cardMessage(type = "SETTLEMENT_MEMO"))
        HttpChatRoomRepository(memo.client()).shareSettlementMemo(21, "점심 45,000원 / 5명")
        assertEquals("/api/v1/chat-rooms/21/messages/settlement-memos", memo.path)
        assertEquals("점심 45,000원 / 5명", JSONObject(memo.body()).getString("memo"))
    }

    /** 투표 본문은 `question`(문서의 `title` 이 아니다) · `options` · `anonymous` 다. */
    @Test
    fun createPollSendsQuestionOptionsAndAnonymous() = runBlocking {
        val call = RecordingCall(pollMessage())
        val repository = HttpChatRoomRepository(call.client())

        repository.createPoll(21, NewPoll(question = "점심 메뉴", options = listOf("한식", "분식"), anonymous = false))

        assertEquals("/api/v1/chat-rooms/21/messages/polls", call.path)
        val body = JSONObject(call.body())
        assertEquals("점심 메뉴", body.getString("question"))
        assertEquals(2, body.getJSONArray("options").length())
        assertEquals("한식", body.getJSONArray("options").getString(0))
        assertFalse(body.getBoolean("anonymous"))
    }

    @Test
    fun voteAndCancelUseSpecPathsAndReturnUpdatedPoll() = runBlocking {
        val vote = RecordingCall(pollMessage(votedOptionId = 23))
        val updated = HttpChatRoomRepository(vote.client()).voteOnPoll(21, 74, 23)

        assertEquals("PUT", vote.method)
        assertEquals("/api/v1/chat-rooms/21/messages/74/poll-options/23/vote", vote.path)
        assertEquals(23L, updated.poll?.myOption?.optionId)

        val cancel = RecordingCall(pollMessage())
        val cleared = HttpChatRoomRepository(cancel.client()).cancelVote(21, 74)

        assertEquals("DELETE", cancel.method)
        assertEquals("/api/v1/chat-rooms/21/messages/74/vote", cancel.path)
        assertNull(cleared.poll?.myOption)
    }

    /** 익명 투표는 `voterNicknames` 가 null 이다 — 빈 목록으로 접으면 "아무도 안 골랐다"와 섞인다. */
    @Test
    fun anonymousPollKeepsVoterNamesNull() = runBlocking {
        val call = RecordingCall(pollMessage())
        val message = HttpChatRoomRepository(call.client()).voteOnPoll(21, 74, 23)

        val poll = message.poll!!
        assertTrue(poll.anonymous)
        assertNull(poll.options.first().voterNicknames)
    }

    @Test
    fun namedPollKeepsVoterNames() = runBlocking {
        val call = RecordingCall(
            """
            {"messageId":74,"type":"POLL","senderId":61,"senderNickname":"즐거운 고양이 4760",
             "content":"투표","createdAt":"2026-08-25T09:00:00","imageUrl":null,
             "poll":{"question":"점심 메뉴","anonymous":false,"totalVoteCount":1,
               "options":[{"optionId":23,"text":"한식","voteCount":1,"votedByMe":true,
                 "voterNicknames":["즐거운 고양이 4760"]}]},"mentions":[]}
            """.trimIndent()
        )
        val message = HttpChatRoomRepository(call.client()).voteOnPoll(21, 74, 23)

        assertEquals(listOf("즐거운 고양이 4760"), message.poll?.options?.first()?.voterNicknames)
    }

    @Test
    fun shareImageSendsMultipartImagePartAndCaption() = runBlocking {
        val call = RecordingCall(cardMessage(type = "IMAGE"))
        val repository = HttpChatRoomRepository(call.client())

        repository.shareImage(
            roomId = 21,
            fileName = "juwangsan.jpg",
            mimeType = "image/jpeg",
            bytes = byteArrayOf(1, 2, 3, 4),
            caption = "주왕산 3폭포"
        )

        assertEquals("/api/v1/chat-rooms/21/messages/images", call.path)
        assertTrue(call.contentType().startsWith("multipart/form-data; boundary=moyeo-"))
        val body = call.body()
        assertTrue(body.contains("name=\"caption\""))
        assertTrue(body.contains("주왕산 3폭포"))
        assertTrue(body.contains("name=\"image\"; filename=\"juwangsan.jpg\""))
        assertTrue(body.contains("Content-Type: image/jpeg"))
        // 바이너리는 그대로 실려야 한다 — 문자열로 다시 인코딩하면 깨진다
        val raw = call.bodyBytes()
        assertTrue(raw.toList().windowed(4).any { it == listOf<Byte>(1, 2, 3, 4) })
    }

    @Test
    fun shareImageOmitsCaptionPartWhenBlank() = runBlocking {
        val call = RecordingCall(cardMessage(type = "IMAGE"))

        HttpChatRoomRepository(call.client())
            .shareImage(21, "a.png", "image/png", byteArrayOf(9), caption = "  ")

        assertFalse(call.body().contains("name=\"caption\""))
    }

    /** 고정만 바꿀 때 `notice` 를 함께 보내면 본문이 덮어써진다 — 넘기지 않은 필드는 넣지 않는다. */
    @Test
    fun noticeUpdateOnlySendsGivenFields() {
        val pinnedOnly = noticeUpdateJson(notice = null, pinned = true)
        assertFalse(pinnedOnly.has("notice"))
        assertTrue(pinnedOnly.getBoolean("pinned"))

        val textOnly = noticeUpdateJson(notice = "집합 장소 변경", pinned = null)
        assertFalse(textOnly.has("pinned"))
        assertEquals("집합 장소 변경", textOnly.getString("notice"))
    }

    @Test
    fun updateNoticeUsesNoticeIdPath() = runBlocking {
        val call = RecordingCall.noContent()
        HttpChatRoomRepository(call.client()).updateNotice(21, 5, pinned = false)

        assertEquals("PUT", call.method)
        assertEquals("/api/v1/chat-rooms/21/notices/5", call.path)
        assertFalse(JSONObject(call.body()).getBoolean("pinned"))
    }

    /** 좌표를 비우려면 명시적 null 이 필요하다 — 필드를 빼면 서버가 기존 값을 유지한다. */
    @Test
    fun meetingInfoUpdateKeepsExplicitNulls() {
        val body = MeetingInfoUpdate(
            meetingDateTime = "2026-09-12T08:30:00",
            meetingLatitude = null,
            meetingLongitude = null,
            meetingDetails = null
        ).toRequestJson()

        assertEquals("2026-09-12T08:30:00", body.getString("meetingDateTime"))
        assertTrue(body.isNull("meetingLatitude"))
        assertTrue(body.isNull("meetingDetails"))
    }

    @Test
    fun updateMeetingInfoAndStatusUseSpecPaths() = runBlocking {
        val meeting = RecordingCall.noContent()
        HttpChatRoomRepository(meeting.client()).updateMeetingInfo(
            21,
            MeetingInfoUpdate("2026-09-12T08:30:00", 36.576, 128.97, "안동역 1번 출구 앞")
        )
        assertEquals("PUT", meeting.method)
        assertEquals("/api/v1/chat-rooms/21/meeting-info", meeting.path)
        assertEquals(36.576, JSONObject(meeting.body()).getDouble("meetingLatitude"), 0.0001)

        val status = RecordingCall.noContent()
        HttpChatRoomRepository(status.client()).changeStatus(21, RoomStatusChange.CANCELLED)
        assertEquals("POST", status.method)
        assertEquals("/api/v1/chat-rooms/21/status", status.path)
        assertEquals("CANCELLED", JSONObject(status.body()).getString("status"))
    }

    /** 매너 점수는 서버가 아직 채우지 않아 null 로 온다 — 파서가 0으로 만들면 화면이 값을 지어낸다. */
    @Test
    fun companionsKeepNullMannerRating() = runBlocking {
        val call = RecordingCall(
            """
            [{"userId":41,"nickname":"숲속여행자","profileImageUrl":null,
              "mannerRating":null,"mannerScore":null,"oneLineReview":null,"reviewed":false}]
            """.trimIndent()
        )
        val companions = HttpChatRoomRepository(call.client()).companions(21)

        assertEquals("/api/v1/chat-rooms/21/companions", call.path)
        assertEquals(41L, companions.single().userId)
        assertNull(companions.single().mannerRating)
        assertNull(companions.single().mannerScore)
        assertFalse(companions.single().reviewed)
    }

    @Test
    fun tripNotCompletedIsNotAnAccessFailure() {
        val call = RecordingCall.failing(409, """{"code":40915,"errorMessage":"아직 완료되지 않은 여행입니다."}""")
        try {
            runBlocking { HttpChatRoomRepository(call.client()).companions(22) }
            fail("409 는 예외로 떠야 합니다.")
        } catch (error: MoyeoApiException) {
            assertTrue(error.tripNotCompleted)
        }
    }

    @Test
    fun reviewCompanionSendsMannerScoreAndOptionalReview() = runBlocking {
        val call = RecordingCall(
            """
            {"userId":41,"nickname":"숲속여행자","profileImageUrl":null,
             "mannerRating":4.8,"mannerScore":5,"oneLineReview":"사진 고마워요!","reviewed":true}
            """.trimIndent()
        )
        val reviewed = HttpChatRoomRepository(call.client()).reviewCompanion(21, 7, 5, "사진 고마워요!")

        assertEquals("PUT", call.method)
        assertEquals("/api/v1/chat-rooms/21/companions/7/review", call.path)
        assertEquals(5, JSONObject(call.body()).getInt("mannerScore"))
        assertEquals("사진 고마워요!", reviewed.oneLineReview)
        assertTrue(reviewed.reviewed)
    }

    @Test
    fun reviewCompanionOmitsBlankOneLineReview() = runBlocking {
        val call = RecordingCall(
            """{"userId":41,"nickname":"숲속여행자","reviewed":true}"""
        )
        HttpChatRoomRepository(call.client()).reviewCompanion(21, 7, 4, "   ")

        assertFalse(JSONObject(call.body()).has("oneLineReview"))
    }

    /** 신청자 정보는 `applicant` 안에 중첩돼 온다 — 평면 필드로 읽으면 전부 빈 값이 된다. */
    @Test
    fun applicationsFlattenNestedApplicant() = runBlocking {
        val call = RecordingCall(
            """
            [{"applicationId":11,"applicationMessage":"단풍 보러 가요","appliedAt":"2026-08-24T10:00:00",
              "applicant":{"userId":77,"nickname":"우직한 곰 7821","profileImageUrl":null,"gender":"M",
                "age":31,"mannerRating":4.9,"completedTripCount":8}}]
            """.trimIndent()
        )
        val applications = HttpChatRoomRepository(call.client()).applications(21)

        assertEquals("/api/v1/chat-rooms/21/applications", call.path)
        val application = applications.single()
        assertEquals(11L, application.applicationId)
        assertEquals(77L, application.userId)
        assertEquals("우직한 곰 7821", application.nickname)
        assertEquals(31, application.age)
        assertEquals(8, application.completedTripCount)
        assertEquals("단풍 보러 가요", application.applicationMessage)
    }

    @Test
    fun approveAndRejectUseSpecPaths() = runBlocking {
        val approve = RecordingCall("""{"applicationId":11,"result":"WAITLISTED","waitlistPosition":2}""")
        val result = HttpChatRoomRepository(approve.client()).approveApplication(21, 11)

        assertEquals("POST", approve.method)
        assertEquals("/api/v1/chat-rooms/21/applications/11/approve", approve.path)
        assertEquals("WAITLISTED", result.result)
        assertEquals(2, result.waitlistPosition)

        val reject = RecordingCall.noContent()
        HttpChatRoomRepository(reject.client()).rejectApplication(21, 11)
        assertEquals("DELETE", reject.method)
        assertEquals("/api/v1/chat-rooms/21/applications/11", reject.path)
    }

    /** 호스트가 나가면 방 자체가 취소된다 — 결과 문자열로 화면이 갈라진다(화면기획 31). */
    @Test
    fun leaveRoomReportsHostCancellation() = runBlocking {
        val call = RecordingCall(
            """{"roomId":21,"result":"HOST_LEFT_AND_ROOM_CANCELLED","promotedUserId":null}"""
        )
        val result = HttpChatRoomRepository(call.client()).leaveRoom(21)

        assertEquals("DELETE", call.method)
        assertEquals("/api/v1/chat-rooms/21/members/me", call.path)
        assertEquals("HOST_LEFT_AND_ROOM_CANCELLED", result.result)
        assertNull(result.promotedUserId)
    }

    @Test
    fun kickMemberSendsReason() = runBlocking {
        val call = RecordingCall.noContent()
        HttpChatRoomRepository(call.client()).kickMember(21, 77, "일정 조건이 맞지 않아요")

        assertEquals("DELETE", call.method)
        assertEquals("/api/v1/chat-rooms/21/members/77", call.path)
        assertEquals("일정 조건이 맞지 않아요", JSONObject(call.body()).getString("reason"))
    }

    /** 목록 조회에서도 카드 본문이 함께 온다 — 채팅 목록이 카드로 그려지는 근거다. */
    @Test
    fun messageListParsesCardPayloads() = runBlocking {
        val call = RecordingCall(
            """
            {"messages":[
              {"messageId":70,"type":"TOURISM_CONTENT","senderId":61,"senderNickname":"나","content":"주산지",
               "createdAt":"2026-08-25T09:00:00","imageUrl":null,
               "tourismContent":{"contentId":126508,"title":"주산지","address":"청송군","thumbnail":null,
                 "latitude":36.4,"longitude":129.0},"mentions":[]},
              {"messageId":71,"type":"IMAGE","senderId":61,"senderNickname":"나","content":"",
               "createdAt":"2026-08-25T09:01:00","imageUrl":"https://cdn.test/a.jpg","mentions":[]}
            ],"nextId":null,"hasNext":false}
            """.trimIndent()
        )
        val page = HttpChatRoomRepository(call.client()).messages(21, limit = 50)

        assertEquals("주산지", page.messages.first().tourismContent?.title)
        assertEquals(126508L, page.messages.first().tourismContent?.contentId)
        assertEquals("https://cdn.test/a.jpg", page.messages[1].imageUrl)
        assertNull(page.messages[1].poll)
    }

    private fun cardMessage(type: String) = """
        {"messageId":74,"type":"$type","senderId":61,"senderNickname":"즐거운 고양이 4760",
         "content":"카드","createdAt":"2026-08-25T09:00:00","imageUrl":null,
         "location":{"latitude":36.576,"longitude":128.97,"name":"안동역 1번 출구 앞"},"mentions":[]}
    """.trimIndent()

    private fun pollMessage(votedOptionId: Long? = null) = """
        {"messageId":74,"type":"POLL","senderId":61,"senderNickname":"즐거운 고양이 4760",
         "content":"투표","createdAt":"2026-08-25T09:00:00","imageUrl":null,
         "poll":{"question":"점심 메뉴","anonymous":true,"totalVoteCount":${if (votedOptionId == null) 0 else 1},
           "options":[
             {"optionId":23,"text":"한식","voteCount":${if (votedOptionId == 23L) 1 else 0},
              "votedByMe":${votedOptionId == 23L},"voterNicknames":null},
             {"optionId":24,"text":"분식","voteCount":0,"votedByMe":false,"voterNicknames":null}
           ]},"mentions":[]}
    """.trimIndent()
}

/** 요청의 메서드·경로·본문을 그대로 붙잡는 연결. 경로가 스펙과 다르면 테스트가 잡아낸다. */
private class RecordingCall private constructor(private val response: String, private val statusCode: Int) {
    lateinit var method: String
        private set
    lateinit var path: String
        private set
    private val recorded = ByteArrayOutputStream()
    private var sentContentType: String? = null

    constructor(response: String) : this(response, HttpURLConnection.HTTP_OK)

    fun body(): String = recorded.toString(Charsets.UTF_8.name())

    fun bodyBytes(): ByteArray = recorded.toByteArray()

    fun contentType(): String = sentContentType.orEmpty()

    fun client() = MoyeoApiClient(
        baseUrl = "https://example.test",
        accessToken = { "access-token" },
        connectionFactory = { url -> Connection(url) }
    )

    private inner class Connection(url: URL) : HttpURLConnection(url) {
        init {
            path = url.path
        }

        override fun setRequestMethod(method: String) {
            super.setRequestMethod(method)
            this@RecordingCall.method = method
        }

        override fun setRequestProperty(key: String, value: String?) {
            super.setRequestProperty(key, value)
            if (key == "Content-Type") sentContentType = value
        }

        override fun disconnect() = Unit

        override fun usingProxy(): Boolean = false

        override fun connect() = Unit

        override fun getResponseCode(): Int = statusCode

        override fun getInputStream(): InputStream = ByteArrayInputStream(response.toByteArray())

        override fun getErrorStream(): InputStream = ByteArrayInputStream(response.toByteArray())

        override fun getOutputStream() = recorded
    }

    companion object {
        fun noContent() = RecordingCall("", HttpURLConnection.HTTP_NO_CONTENT)

        fun failing(statusCode: Int, body: String) = RecordingCall(body, statusCode)
    }
}
