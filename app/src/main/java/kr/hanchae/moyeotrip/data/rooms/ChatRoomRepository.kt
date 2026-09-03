package kr.hanchae.moyeotrip.data.rooms

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.MultipartFile
import kr.hanchae.moyeotrip.data.api.doubleOrNull
import kr.hanchae.moyeotrip.data.api.intOrNull
import kr.hanchae.moyeotrip.data.api.longOrNull
import kr.hanchae.moyeotrip.data.api.mapArray
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import org.json.JSONArray
import org.json.JSONObject

data class RoomTag(val tagId: Long, val name: String)

/**
 * GET chat-rooms/search 항목. 2026-08-24 서버 패치로 카드 표기(찜·상태·모집 마감 D-day)와
 * 지도 마커(집합 좌표)·집합 안내·당일 여행 시간이 목록 응답에 함께 온다 — 항목마다 상세를 부르지 않는다.
 *
 * `dayTripStartTime`/`dayTripEndTime` 은 문서엔 `HH:mm` 인데 실응답이 `HH:mm:ss` 라 원문을 그대로 담고,
 * 표기는 [dayTripHoursText] 가 `HH:mm` 으로 정규화한다.
 */
data class ChatRoomSearchResult(
    val roomId: Long,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val tripType: String,
    val startDate: String,
    val endDate: String?,
    val dayTripStartTime: String?,
    val dayTripEndTime: String?,
    val recruitmentDeadlineDate: String?,
    val recruitmentDDay: Int?,
    val status: String,
    val favorite: Boolean,
    val meetingLatitude: Double?,
    val meetingLongitude: Double?,
    val meetingDetails: String?,
    val meetingDateTime: String?,
    val participantCount: Int,
    val maxParticipants: Int,
    val courseTitle: String?,
    val tags: List<RoomTag>
)

data class ChatRoomDetail(
    val roomId: Long,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val tripType: String,
    val startDate: String,
    val endDate: String?,
    val recruitmentDeadlineDate: String?,
    val tripNights: Int,
    val tripDays: Int,
    val dayTripStartTime: String?,
    val dayTripEndTime: String?,
    val meetingLatitude: Double?,
    val meetingLongitude: Double?,
    val meetingDetails: String?,
    val meetingDateTime: String?,
    val participationFee: Int?,
    val genderRestriction: String?,
    val minimumAge: Int?,
    val maximumAge: Int?,
    val recruitmentDDay: Int?,
    val hostId: Long?,
    val hostProfileImageUrl: String?,
    val participantCount: Int,
    // 18-1 확정 CTA 를 잠그는 근거. 서버 상세 응답의 `minimumParticipants` 를 그대로 쓴다 —
    // 이 값이 없다고 임의의 하한(2·3명)을 지어내지 않는다.
    val minimumParticipants: Int?,
    val maxParticipants: Int,
    val status: String,
    val favorite: Boolean,
    val participantImageUrls: List<String?>,
    // 2026-08-26 BE 변경: 검색 응답이 카드용으로 축소되면서 코스 제목·태그가 상세로 옮겨졌다.
    // canApply 는 삭제된 join-eligibility 를 대체한다(모집 상태·마감·기존 신청·성별/나이 제한이 반영된 값).
    val courseTitle: String?,
    val tags: List<RoomTag>,
    val canApply: Boolean?
)

enum class RoomApplicationResult { JOINED, WAITLISTED, PENDING_APPROVAL }

/**
 * POST chat-rooms 요청값 (화면기획 17 모집 만들기).
 *
 * 서버는 당일치기/숙박을 **상호배타**로 검증한다(에러코드 40008):
 * `DAY_TRIP` 은 `endDate` 없이 시작·종료 시각을, 1박 이상은 `endDate` 만 보낸다.
 * 조립은 [toRequestJson] 이 강제하므로 호출부가 규칙을 다시 신경 쓰지 않아도 된다.
 *
 * `courseType` 은 필수이며 `PUBLIC` 은 [courseId] 하나만, `CUSTOM` 은 커스텀 코스만 보낸다
 * (둘을 함께 보내면 40909). 커스텀 코스는 방문지의 서버 `contentId` 가 필요해 이번 연동 범위 밖이다.
 */
data class NewChatRoom(
    val title: String,
    val description: String?,
    val maxParticipants: Int,
    val dayTrip: Boolean,
    val startDate: String,
    val endDate: String?,
    val dayTripStartTime: String?,
    val dayTripEndTime: String?,
    val recruitmentDeadlineDate: String,
    val meetingLatitude: Double?,
    val meetingLongitude: Double?,
    val meetingDetails: String?,
    val meetingDateTime: String,
    val participationFee: Int?,
    val genderRestriction: String,
    val minimumAge: Int?,
    val maximumAge: Int?,
    val joinApprovalMode: String,
    val publicCourseId: Long
)

/** 서버 검증(40008·40909)을 그대로 지키는 요청 본문. 규칙에 맞지 않는 필드는 아예 넣지 않는다. */
fun NewChatRoom.toRequestJson(): JSONObject {
    val json = JSONObject()
        .put("title", title)
        .put("maxParticipants", maxParticipants)
        .put("tripType", if (dayTrip) "DAY_TRIP" else "OVERNIGHT")
        .put("startDate", startDate)
        .put("recruitmentDeadlineDate", recruitmentDeadlineDate)
        .put("meetingDateTime", meetingDateTime)
        .put("genderRestriction", genderRestriction)
        .put("joinApprovalMode", joinApprovalMode)
        .put("courseType", "PUBLIC")
        .put("courseId", publicCourseId)
    description?.takeIf(String::isNotBlank)?.let { json.put("description", it) }
    if (dayTrip) {
        dayTripStartTime?.let { json.put("dayTripStartTime", it) }
        dayTripEndTime?.let { json.put("dayTripEndTime", it) }
    } else {
        endDate?.let { json.put("endDate", it) }
    }
    meetingLatitude?.let { json.put("meetingLatitude", it) }
    meetingLongitude?.let { json.put("meetingLongitude", it) }
    meetingDetails?.takeIf(String::isNotBlank)?.let { json.put("meetingDetails", it) }
    participationFee?.let { json.put("participationFee", it) }
    minimumAge?.let { json.put("minimumAge", it) }
    maximumAge?.let { json.put("maximumAge", it) }
    return json
}

data class MyChatRoom(
    val roomId: Long,
    val courseId: Long?,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String?,
    val thumbnail: String?,
    val status: String,
    val recruitmentDDay: Int?,
    val ended: Boolean,
    val participantCount: Int?,
    val maxParticipants: Int?,
    val unreadMessageCount: Int?,
    val latestMessage: String?
)

data class MyWaitingRoom(
    val roomId: Long,
    val title: String,
    val thumbnail: String?,
    val applicationStatus: String,
    val waitlistPosition: Int?,
    val tripType: String,
    val startDate: String,
    val endDate: String?,
    val participantCount: Int,
    val maxParticipants: Int
)

data class RoomKickHistory(
    val kickHistoryId: Long,
    val roomId: Long,
    val roomTitle: String,
    val reason: String,
    val kickedAt: String
)

/** GET chat-rooms/{id}/members — 방 참여자만 200, 비참여자는 403. */
data class RoomMember(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val completedTripCount: Int,
    val host: Boolean,
    val me: Boolean
)

data class RoomMembers(
    val participantCount: Int,
    val maxParticipants: Int,
    val waitlistCount: Int,
    val members: List<RoomMember>
)

data class RoomNotice(
    val noticeId: Long,
    val content: String?,
    val pinned: Boolean,
    val authorNickname: String,
    val createdAt: String
)

data class RoomNotices(val pinned: List<RoomNotice>, val unpinned: List<RoomNotice>) {
    val all: List<RoomNotice> get() = pinned + unpinned
}

/**
 * 방 하나에 둘 수 있는 상단 고정 공지 수 — **1개**다 (`ATTACH-COMPOSER-CANON.md` R5-1).
 *
 * 서버는 고정 개수를 **제한하지 않는다**(실서버 방 101 에 2건이 고정돼 있는 것을 확인했다).
 * 그러니 클라이언트가 지켜야 한다 — 새로 고정할 때 기존 고정 공지를
 * `PUT chat-rooms/{id}/notices/{noticeId}` 로 `pinned:false` 처리한다.
 */
const val ROOM_NOTICE_PIN_LIMIT = 1

data class RoadmapPlace(
    val contentId: Long,
    val sequence: Int,
    val title: String,
    val thumbnail: String?,
    val latitude: Double?,
    val longitude: Double?,
    val scheduledAt: String?,
    val progress: String
)

data class RoomRoadmap(val active: Boolean, val dayNumber: Int?, val totalDays: Int, val places: List<RoadmapPlace>)

/** LOCATION 메시지의 카드 본문 — 호스트가 등록한 집합 좌표를 그대로 공유한다. */
data class SharedLocation(val latitude: Double, val longitude: Double, val name: String?)

/** TOURISM_CONTENT 메시지의 카드 본문. */
data class SharedTourismContent(
    val contentId: Long,
    val title: String,
    val address: String?,
    val thumbnail: String?,
    val latitude: Double?,
    val longitude: Double?
)

/**
 * POLL 메시지의 선택지. [voterNicknames] 는 실명 투표에서만 오고 익명이면 null 이다 —
 * null 과 빈 목록은 뜻이 달라서 구분해 둔다.
 */
data class PollOption(
    val optionId: Long,
    val text: String,
    val voteCount: Int,
    val votedByMe: Boolean,
    val voterNicknames: List<String>?
)

data class MessagePoll(
    val question: String,
    val anonymous: Boolean,
    val totalVoteCount: Int,
    val options: List<PollOption>
) {
    /** 내가 고른 선택지. 없으면 아직 투표하지 않은 것이다 — 투표 취소 버튼의 표시 조건이다. */
    val myOption: PollOption? get() = options.firstOrNull(PollOption::votedByMe)
}

data class RoomMessage(
    val messageId: Long,
    val type: String,
    val senderId: Long?,
    val senderNickname: String,
    val content: String,
    val createdAt: String,
    val imageUrl: String?,
    val location: SharedLocation? = null,
    val tourismContent: SharedTourismContent? = null,
    val poll: MessagePoll? = null
)

data class RoomMessagePage(val messages: List<RoomMessage>, val nextId: Long?, val hasNext: Boolean)

/** 새 투표 요청 본문. 서버는 선택지를 2~5개로 검증한다. */
data class NewPoll(val question: String, val options: List<String>, val anonymous: Boolean = true)

/** PUT chat-rooms/{id}/meeting-info 요청값. `meetingDateTime` 만 필수다. */
data class MeetingInfoUpdate(
    val meetingDateTime: String,
    val meetingLatitude: Double?,
    val meetingLongitude: Double?,
    val meetingDetails: String?
)

/** POST chat-rooms/{id}/status 로 보낼 수 있는 상태. */
enum class RoomStatusChange { RECRUITING, CONFIRMED, CANCELLED }

/**
 * GET chat-rooms/{id}/companions — 완료 여행 전용이다.
 * 미완료 방은 `409 40915`([MoyeoApiException.tripNotCompleted])를 준다.
 * [mannerRating] 은 서버가 아직 값을 채우지 않아 대개 null 이다 — null 이면 표기를 숨긴다.
 */
data class RoomCompanion(
    // 2026-08-26 BE 변경: companionRecordId 가 응답에서 제거됐다.
    // 평가 API 의 경로 변수도 companionUserId 로 바뀌어, 식별자는 userId 하나로 통일됐다.
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val mannerRating: Double?,
    val mannerScore: Int?,
    val oneLineReview: String?,
    val reviewed: Boolean
)

/** GET chat-rooms/{id}/applications 항목 (화면기획 18 승인 대기). */
data class RoomApplication(
    val applicationId: Long,
    val applicationMessage: String?,
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val gender: String?,
    val age: Int?,
    val mannerRating: Double?,
    val completedTripCount: Int,
    val appliedAt: String
)

/** POST applications/{id}/approve 응답 — 정원이 찼으면 대기열로 넘어간다. */
data class ApplicationApproval(val applicationId: Long, val result: String, val waitlistPosition: Int?)

/**
 * DELETE chat-rooms/{id}/members/me 응답.
 * 호스트가 나가면 `HOST_LEFT_AND_ROOM_CANCELLED` 로 방 자체가 취소된다(화면기획 31).
 */
data class RoomLeaveResult(val roomId: Long, val result: String, val promotedUserId: Long?)

interface ChatRoomRepository {
    /**
     * 12 · 12-1 통합 모집 검색 — `GET /api/v1/chat-rooms/search?keyword=`.
     *
     * 서버가 채팅방 제목·소개, 코스 이름, 코스 태그, 방문지 이름·주소를 한 번에 찾는다.
     * **사용자가 직접 입력한 검색어**만 넘긴다 — 태그를 눌러 찾는 기능은 없다(2026-08-31 확인).
     */
    suspend fun search(keyword: String? = null, limit: Int? = null): List<ChatRoomSearchResult>

    /**
     * 지도 반경 조회 — `GET /api/v1/chat-rooms/map`.
     *
     * **검색 응답(`/search`)에는 집합 좌표가 없다**(2026-08-26 응답 축소). 지도에 핀을 찍으려면
     * 이 엔드포인트를 써야 한다. 좌표 없는 목록으로 지도를 그리려 하면 늘 목업으로 떨어진다.
     */
    suspend fun mapRooms(latitude: Double, longitude: Double, radiusKm: Double): List<ChatRoomSearchResult>

    /**
     * 생성된 방의 roomId — 201 `CreateChatRoomResponse`. 이 값으로 15 모집 상세로 이동한다.
     *
     * 2026-08-26 서버 변경: 썸네일이 **필수**다(없으면 400 `40041`,
     * 이미지가 아니거나 20MB 초과면 400 `40042`). 서버가 저장 전에 비율 유지 FHD 축소 + WebP 변환을
     * 직접 하므로(2026-08-29 안내) 클라에서 리사이즈·변환하지 않는다.
     * 사용자가 사진을 고르지 않으면 호출부가 기본 플레이스홀더를 넘긴다.
     */
    suspend fun createRoom(room: NewChatRoom, thumbnail: MultipartFile): Long

    suspend fun room(roomId: Long): ChatRoomDetail

    suspend fun apply(roomId: Long, message: String?): RoomApplicationResult

    suspend fun cancelApplication(roomId: Long)

    suspend fun toggleFavorite(roomId: Long): Boolean

    /**
     * 26-1 찜한 모집 — `GET /api/v1/chat-rooms/my/favorites`.
     * 마이 26 의 `찜한 코스` 와 다른 목록이다. 코스는 코스고 모집은 모집이다.
     */
    suspend fun favoriteRooms(): List<ChatRoomSearchResult>

    suspend fun myRooms(): List<MyChatRoom>

    suspend fun myWaitingRooms(): List<MyWaitingRoom>

    suspend fun myKickHistories(): List<RoomKickHistory>

    suspend fun members(roomId: Long): RoomMembers

    suspend fun notices(roomId: Long): RoomNotices

    suspend fun currentRoadmap(roomId: Long): RoomRoadmap

    suspend fun messages(roomId: Long, beforeMessageId: Long? = null, limit: Int? = null): RoomMessagePage

    suspend fun sendMessage(roomId: Long, content: String): RoomMessage

    /** 20-2 사진 공유 — multipart(`image` 파일 파트 + 선택 `caption`). */
    suspend fun shareImage(
        roomId: Long,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        caption: String? = null
    ): RoomMessage

    /** 20-2 지도(만날 위치) 공유 — 본문이 없다. 방에 집합 좌표가 없으면 400 이다. */
    suspend fun shareMeetingLocation(roomId: Long): RoomMessage

    /** 20-2 장소 공유 — TourAPI contentId 를 카드로 보낸다. */
    suspend fun shareTourismContent(roomId: Long, contentId: Long): RoomMessage

    /** 20-2 정산 메모 공유 — 송금 없는 메모 한 줄. */
    suspend fun shareSettlementMemo(roomId: Long, memo: String): RoomMessage

    /** 20-2 투표 개최 — 선택지 2~5개. */
    suspend fun createPoll(roomId: Long, poll: NewPoll): RoomMessage

    /** 20 투표 참여 — 이미 고른 선택지가 있으면 서버가 그 표를 옮긴다. */
    suspend fun voteOnPoll(roomId: Long, messageId: Long, optionId: Long): RoomMessage

    /** 20 투표 취소. */
    suspend fun cancelVote(roomId: Long, messageId: Long): RoomMessage

    /** 17-3 집합 장소·시간 수정 (호스트). */
    suspend fun updateMeetingInfo(roomId: Long, update: MeetingInfoUpdate)

    /** 18 모집 확정·취소 (호스트). */
    suspend fun changeStatus(roomId: Long, status: RoomStatusChange)

    /** 20-3 공지 등록 (호스트). 고정 개수는 서버가 막지 않는다 — [publishNotice] 를 쓴다. */
    suspend fun createNotice(roomId: Long, notice: String, pinned: Boolean)

    /** 20-3 공지 수정·고정 토글 (호스트). null 인 필드는 보내지 않아 그대로 유지된다. */
    suspend fun updateNotice(roomId: Long, noticeId: Long, notice: String? = null, pinned: Boolean? = null)

    /** 20-3a 공지 삭제 — `DELETE /api/v1/chat-rooms/{roomId}/notices/{noticeId}`. 되돌릴 수 없다. */
    suspend fun deleteNotice(roomId: Long, noticeId: Long)

    /** 20-1 · 27 동행자 목록 — 완료 여행만 200, 미완료는 `409 40915`. */
    suspend fun companions(roomId: Long): List<RoomCompanion>

    /** 27-3 동행자 평가 — `mannerScore` 가 필수다. 경로 변수는 상대의 `userId` 다. */
    suspend fun reviewCompanion(
        roomId: Long,
        companionUserId: Long,
        mannerScore: Int,
        oneLineReview: String? = null
    ): RoomCompanion

    /** 18 승인 대기 목록 (호스트). */
    suspend fun applications(roomId: Long): List<RoomApplication>

    /** 18 신청 승인 (호스트). */
    suspend fun approveApplication(roomId: Long, applicationId: Long): ApplicationApproval

    /** 18 신청 거절 (호스트). */
    suspend fun rejectApplication(roomId: Long, applicationId: Long)

    /** 20-1 채팅방 나가기 · 31 확인. 호스트가 나가면 방이 취소된다. */
    suspend fun leaveRoom(roomId: Long): RoomLeaveResult

    /** 20-1b 멤버 내보내기 (호스트). 사유는 필수이고 상대 알림에 그대로 전달된다. */
    suspend fun kickMember(roomId: Long, memberId: Long, reason: String)
}

class HttpChatRoomRepository(private val client: MoyeoApiClient) : ChatRoomRepository {
    override suspend fun search(keyword: String?, limit: Int?): List<ChatRoomSearchResult> {
        val query = buildList {
            keyword?.takeIf(String::isNotBlank)?.let { add("keyword=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            limit?.let { add("limit=$it") }
        }.joinToString("&")
        val path = "/api/v1/chat-rooms/search" + if (query.isBlank()) "" else "?$query"
        return client.getArray(path).mapObjects(JSONObject::toChatRoomSearchResult)
    }

    override suspend fun mapRooms(latitude: Double, longitude: Double, radiusKm: Double): List<ChatRoomSearchResult> =
        client
            .getArray("/api/v1/chat-rooms/map?latitude=$latitude&longitude=$longitude&radiusKm=$radiusKm")
            .mapObjects(JSONObject::toChatRoomSearchResult)

    /** multipart 요청이고 `request` 파트가 application/json 이다. 썸네일은 선택이라 보내지 않는다. */
    override suspend fun createRoom(room: NewChatRoom, thumbnail: MultipartFile): Long =
        client.sendMultipartJsonAndFileForObject(
            "POST",
            "/api/v1/chat-rooms",
            "request",
            room.toRequestJson(),
            thumbnail
        ).getLong("roomId")

    override suspend fun room(roomId: Long): ChatRoomDetail =
        client.getObject("/api/v1/chat-rooms/$roomId").toRoomDetail()

    override suspend fun apply(roomId: Long, message: String?): RoomApplicationResult {
        val body = JSONObject()
        message?.takeIf(String::isNotBlank)?.let { body.put("applicationMessage", it) }
        val json = client.sendForObject("POST", "/api/v1/chat-rooms/$roomId/applications", body)
        return RoomApplicationResult.valueOf(json.getString("result"))
    }

    override suspend fun cancelApplication(roomId: Long) {
        client.send("DELETE", "/api/v1/chat-rooms/$roomId/applications/me")
    }

    override suspend fun toggleFavorite(roomId: Long): Boolean =
        client.sendForObject("POST", "/api/v1/chat-rooms/$roomId/favorite").optBoolean("favorite")

    override suspend fun favoriteRooms(): List<ChatRoomSearchResult> =
        client.getArray("/api/v1/chat-rooms/my/favorites").mapObjects(JSONObject::toChatRoomSearchResult)

    override suspend fun myRooms(): List<MyChatRoom> =
        client.getArray("/api/v1/chat-rooms/my").mapObjects(JSONObject::toMyRoom)

    override suspend fun myWaitingRooms(): List<MyWaitingRoom> =
        client.getArray("/api/v1/chat-rooms/my-waiting").mapObjects(JSONObject::toWaitingRoom)

    override suspend fun myKickHistories(): List<RoomKickHistory> =
        client.getArray("/api/v1/chat-rooms/my-kick-histories").mapObjects(JSONObject::toKickHistory)

    override suspend fun members(roomId: Long): RoomMembers {
        val json = client.getObject("/api/v1/chat-rooms/$roomId/members")
        return RoomMembers(
            participantCount = json.optInt("participantCount"),
            maxParticipants = json.optInt("maxParticipants"),
            waitlistCount = json.optInt("waitlistCount"),
            members = json.mapArray("members", JSONObject::toRoomMember)
        )
    }

    override suspend fun notices(roomId: Long): RoomNotices {
        val json = client.getObject("/api/v1/chat-rooms/$roomId/notices")
        return RoomNotices(
            pinned = json.mapArray("pinnedNotices", JSONObject::toRoomNotice),
            unpinned = json.mapArray("unpinnedNotices", JSONObject::toRoomNotice)
        )
    }

    override suspend fun currentRoadmap(roomId: Long): RoomRoadmap {
        val json = client.getObject("/api/v1/chat-rooms/$roomId/roadmap/current")
        return RoomRoadmap(
            active = json.optBoolean("active"),
            dayNumber = json.intOrNull("dayNumber"),
            totalDays = json.optInt("totalDays"),
            places = json.mapArray("places", JSONObject::toRoadmapPlace)
                .sortedBy(RoadmapPlace::sequence)
        )
    }

    override suspend fun messages(roomId: Long, beforeMessageId: Long?, limit: Int?): RoomMessagePage {
        val query = buildList {
            beforeMessageId?.let { add("beforeMessageId=$it") }
            limit?.let { add("limit=$it") }
        }.joinToString("&")
        val path = "/api/v1/chat-rooms/$roomId/messages" + if (query.isBlank()) "" else "?$query"
        val json = client.getObject(path)
        return RoomMessagePage(
            messages = json.mapArray("messages", JSONObject::toRoomMessage),
            nextId = json.longOrNull("nextId"),
            hasNext = json.optBoolean("hasNext")
        )
    }

    /** 스펙의 필수 필드는 content·mentionedUserIds 다 — 멘션이 없어도 빈 배열을 함께 보낸다. */
    override suspend fun sendMessage(roomId: Long, content: String): RoomMessage {
        val body = JSONObject()
            .put("content", content)
            .put("mentionedUserIds", JSONArray())
        return client.sendForObject("POST", "/api/v1/chat-rooms/$roomId/messages", body).toRoomMessage()
    }

    override suspend fun shareImage(
        roomId: Long,
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
        caption: String?
    ): RoomMessage = client.sendMultipartFileForObject(
        method = "POST",
        path = "/api/v1/chat-rooms/$roomId/messages/images",
        file = MultipartFile(partName = "image", fileName = fileName, mimeType = mimeType, bytes = bytes),
        textParts = caption?.takeIf(String::isNotBlank)?.let { mapOf("caption" to it) }.orEmpty()
    ).toRoomMessage()

    /** 본문이 없는 요청이다 — 빈 JSON 을 보내면 서버가 400 을 줄 수 있어 payload 를 아예 붙이지 않는다. */
    override suspend fun shareMeetingLocation(roomId: Long): RoomMessage =
        client.sendForObject("POST", "/api/v1/chat-rooms/$roomId/messages/locations").toRoomMessage()

    override suspend fun shareTourismContent(roomId: Long, contentId: Long): RoomMessage = client.sendForObject(
        "POST",
        "/api/v1/chat-rooms/$roomId/messages/tourism-contents",
        JSONObject().put("contentId", contentId)
    ).toRoomMessage()

    override suspend fun shareSettlementMemo(roomId: Long, memo: String): RoomMessage = client.sendForObject(
        "POST",
        "/api/v1/chat-rooms/$roomId/messages/settlement-memos",
        JSONObject().put("memo", memo)
    ).toRoomMessage()

    override suspend fun createPoll(roomId: Long, poll: NewPoll): RoomMessage =
        client.sendForObject("POST", "/api/v1/chat-rooms/$roomId/messages/polls", poll.toRequestJson())
            .toRoomMessage()

    override suspend fun voteOnPoll(roomId: Long, messageId: Long, optionId: Long): RoomMessage = client.sendForObject(
        "PUT",
        "/api/v1/chat-rooms/$roomId/messages/$messageId/poll-options/$optionId/vote"
    ).toRoomMessage()

    override suspend fun cancelVote(roomId: Long, messageId: Long): RoomMessage =
        client.sendForObject("DELETE", "/api/v1/chat-rooms/$roomId/messages/$messageId/vote").toRoomMessage()

    override suspend fun updateMeetingInfo(roomId: Long, update: MeetingInfoUpdate) {
        client.send("PUT", "/api/v1/chat-rooms/$roomId/meeting-info", update.toRequestJson())
    }

    override suspend fun changeStatus(roomId: Long, status: RoomStatusChange) {
        client.send("POST", "/api/v1/chat-rooms/$roomId/status", JSONObject().put("status", status.name))
    }

    override suspend fun createNotice(roomId: Long, notice: String, pinned: Boolean) {
        client.send(
            "POST",
            "/api/v1/chat-rooms/$roomId/notices",
            JSONObject().put("notice", notice).put("pinned", pinned)
        )
    }

    override suspend fun updateNotice(roomId: Long, noticeId: Long, notice: String?, pinned: Boolean?) {
        client.send(
            "PUT",
            "/api/v1/chat-rooms/$roomId/notices/$noticeId",
            noticeUpdateJson(notice = notice, pinned = pinned)
        )
    }

    override suspend fun deleteNotice(roomId: Long, noticeId: Long) {
        client.send("DELETE", "/api/v1/chat-rooms/$roomId/notices/$noticeId")
    }

    override suspend fun companions(roomId: Long): List<RoomCompanion> =
        client.getArray("/api/v1/chat-rooms/$roomId/companions").mapObjects(JSONObject::toCompanion)

    override suspend fun reviewCompanion(
        roomId: Long,
        companionUserId: Long,
        mannerScore: Int,
        oneLineReview: String?
    ): RoomCompanion {
        val body = JSONObject().put("mannerScore", mannerScore)
        oneLineReview?.takeIf(String::isNotBlank)?.let { body.put("oneLineReview", it) }
        return client.sendForObject(
            "PUT",
            "/api/v1/chat-rooms/$roomId/companions/$companionUserId/review",
            body
        ).toCompanion()
    }

    override suspend fun applications(roomId: Long): List<RoomApplication> =
        client.getArray("/api/v1/chat-rooms/$roomId/applications").mapObjects(JSONObject::toApplication)

    override suspend fun approveApplication(roomId: Long, applicationId: Long): ApplicationApproval {
        val json = client.sendForObject(
            "POST",
            "/api/v1/chat-rooms/$roomId/applications/$applicationId/approve"
        )
        return ApplicationApproval(
            applicationId = json.optLong("applicationId", applicationId),
            result = json.optString("result"),
            waitlistPosition = json.intOrNull("waitlistPosition")
        )
    }

    override suspend fun rejectApplication(roomId: Long, applicationId: Long) {
        client.send("DELETE", "/api/v1/chat-rooms/$roomId/applications/$applicationId")
    }

    override suspend fun leaveRoom(roomId: Long): RoomLeaveResult {
        val json = client.sendForObject("DELETE", "/api/v1/chat-rooms/$roomId/members/me")
        return RoomLeaveResult(
            roomId = json.optLong("roomId", roomId),
            result = json.optString("result"),
            promotedUserId = json.longOrNull("promotedUserId")
        )
    }

    override suspend fun kickMember(roomId: Long, memberId: Long, reason: String) {
        client.send(
            "DELETE",
            "/api/v1/chat-rooms/$roomId/members/$memberId",
            JSONObject().put("reason", reason)
        )
    }
}

/** 선택지는 문자열 배열이고 익명 여부는 필수다(생략하면 서버 기본이 익명). */
internal fun NewPoll.toRequestJson(): JSONObject = JSONObject()
    .put("question", question)
    .put("options", JSONArray(options))
    .put("anonymous", anonymous)

/** `meetingDateTime` 만 필수다. 나머지는 null 이면 명시적으로 null 을 보내 서버가 값을 비운다. */
internal fun MeetingInfoUpdate.toRequestJson(): JSONObject = JSONObject()
    .put("meetingDateTime", meetingDateTime)
    .put("meetingLatitude", meetingLatitude ?: JSONObject.NULL)
    .put("meetingLongitude", meetingLongitude ?: JSONObject.NULL)
    .put("meetingDetails", meetingDetails ?: JSONObject.NULL)

/**
 * 공지 수정 본문. 고정만 토글할 때 `notice` 를 함께 보내면 본문이 덮어써지므로
 * 넘기지 않은 필드는 아예 넣지 않는다.
 */
internal fun noticeUpdateJson(notice: String?, pinned: Boolean?): JSONObject {
    val body = JSONObject()
    notice?.let { body.put("notice", it) }
    pinned?.let { body.put("pinned", it) }
    return body
}

/**
 * `SearchChatRoomResponse` → [ChatRoomSearchResult].
 * 코스로 열린 모집(GET travel-courses/{id}/chat-rooms)도 같은 모양이라 함께 쓴다.
 */
internal fun JSONObject.toChatRoomSearchResult() = ChatRoomSearchResult(
    roomId = getLong("roomId"),
    title = getString("title"),
    description = stringOrNull("description"),
    thumbnail = stringOrNull("thumbnail"),
    tripType = optString("tripType"),
    startDate = optString("startDate"),
    endDate = stringOrNull("endDate"),
    dayTripStartTime = stringOrNull("dayTripStartTime"),
    dayTripEndTime = stringOrNull("dayTripEndTime"),
    recruitmentDeadlineDate = stringOrNull("recruitmentDeadlineDate"),
    recruitmentDDay = intOrNull("recruitmentDDay"),
    status = optString("status"),
    favorite = optBoolean("favorite"),
    meetingLatitude = doubleOrNull("meetingLatitude"),
    meetingLongitude = doubleOrNull("meetingLongitude"),
    meetingDetails = stringOrNull("meetingDetails"),
    meetingDateTime = stringOrNull("meetingDateTime"),
    participantCount = optInt("participantCount"),
    maxParticipants = optInt("maxParticipants"),
    courseTitle = stringOrNull("courseTitle"),
    tags = mapArray("tags") { RoomTag(it.getLong("tagId"), it.getString("name")) }
)

private fun JSONObject.toRoomDetail() = ChatRoomDetail(
    roomId = getLong("roomId"),
    title = getString("title"),
    description = stringOrNull("description"),
    thumbnail = stringOrNull("thumbnail"),
    tripType = optString("tripType"),
    startDate = optString("startDate"),
    endDate = stringOrNull("endDate"),
    recruitmentDeadlineDate = stringOrNull("recruitmentDeadlineDate"),
    tripNights = optInt("tripNights"),
    tripDays = optInt("tripDays"),
    dayTripStartTime = stringOrNull("dayTripStartTime"),
    dayTripEndTime = stringOrNull("dayTripEndTime"),
    meetingLatitude = doubleOrNull("meetingLatitude"),
    meetingLongitude = doubleOrNull("meetingLongitude"),
    meetingDetails = stringOrNull("meetingDetails"),
    meetingDateTime = stringOrNull("meetingDateTime"),
    participationFee = intOrNull("participationFee"),
    genderRestriction = stringOrNull("genderRestriction"),
    minimumAge = intOrNull("minimumAge"),
    maximumAge = intOrNull("maximumAge"),
    recruitmentDDay = intOrNull("recruitmentDDay"),
    hostId = longOrNull("hostId"),
    hostProfileImageUrl = stringOrNull("hostProfileImageUrl"),
    participantCount = optInt("participantCount"),
    minimumParticipants = intOrNull("minimumParticipants"),
    maxParticipants = optInt("maxParticipants"),
    status = optString("status"),
    favorite = optBoolean("favorite"),
    participantImageUrls = mapArray("participants") { it.stringOrNull("profileImageUrl") },
    courseTitle = stringOrNull("courseTitle"),
    tags = mapArray("tags") { RoomTag(it.getLong("tagId"), it.getString("name")) },
    canApply = if (has("canApply") && !isNull("canApply")) optBoolean("canApply") else null
)

private fun JSONObject.toMyRoom() = MyChatRoom(
    roomId = getLong("roomId"),
    courseId = if (has("courseId") && !isNull("courseId")) optLong("courseId") else null,
    title = getString("title"),
    description = stringOrNull("description"),
    startDate = optString("startDate"),
    endDate = stringOrNull("endDate"),
    thumbnail = stringOrNull("thumbnail"),
    status = optString("status"),
    recruitmentDDay = intOrNull("recruitmentDDay"),
    ended = optBoolean("ended"),
    participantCount = intOrNull("participantCount"),
    maxParticipants = intOrNull("maxParticipants"),
    unreadMessageCount = intOrNull("unreadMessageCount"),
    latestMessage = optJSONObject("latestMessage")?.stringOrNull("content")
)

private fun JSONObject.toWaitingRoom() = MyWaitingRoom(
    roomId = getLong("roomId"),
    title = getString("title"),
    thumbnail = stringOrNull("thumbnail"),
    applicationStatus = optString("applicationStatus"),
    waitlistPosition = intOrNull("waitlistPosition"),
    tripType = optString("tripType"),
    startDate = optString("startDate"),
    endDate = stringOrNull("endDate"),
    participantCount = optInt("participantCount"),
    maxParticipants = optInt("maxParticipants")
)

private fun JSONObject.toRoomMember() = RoomMember(
    userId = getLong("userId"),
    nickname = optString("nickname"),
    profileImageUrl = stringOrNull("profileImageUrl"),
    completedTripCount = optInt("completedTripCount"),
    host = optBoolean("host"),
    me = optBoolean("me")
)

private fun JSONObject.toRoomNotice() = RoomNotice(
    noticeId = getLong("noticeId"),
    content = stringOrNull("content"),
    pinned = optBoolean("pinned"),
    authorNickname = optString("authorNickname"),
    createdAt = optString("createdAt")
)

private fun JSONObject.toRoadmapPlace() = RoadmapPlace(
    contentId = optLong("contentId"),
    sequence = optInt("sequence"),
    title = optString("title"),
    thumbnail = stringOrNull("thumbnail"),
    latitude = doubleOrNull("latitude"),
    longitude = doubleOrNull("longitude"),
    scheduledAt = stringOrNull("scheduledAt"),
    progress = optString("progress")
)

private fun JSONObject.toRoomMessage() = RoomMessage(
    messageId = getLong("messageId"),
    type = optString("type"),
    senderId = longOrNull("senderId"),
    senderNickname = optString("senderNickname"),
    content = optString("content"),
    createdAt = optString("createdAt"),
    imageUrl = stringOrNull("imageUrl"),
    location = optJSONObject("location")?.toSharedLocation(),
    tourismContent = optJSONObject("tourismContent")?.toSharedTourismContent(),
    poll = optJSONObject("poll")?.toMessagePoll()
)

private fun JSONObject.toSharedLocation() = SharedLocation(
    latitude = optDouble("latitude"),
    longitude = optDouble("longitude"),
    name = stringOrNull("name")
)

private fun JSONObject.toSharedTourismContent() = SharedTourismContent(
    contentId = optLong("contentId"),
    title = optString("title"),
    address = stringOrNull("address"),
    thumbnail = stringOrNull("thumbnail"),
    latitude = doubleOrNull("latitude"),
    longitude = doubleOrNull("longitude")
)

private fun JSONObject.toMessagePoll() = MessagePoll(
    question = optString("question"),
    anonymous = optBoolean("anonymous"),
    totalVoteCount = optInt("totalVoteCount"),
    options = mapArray("options", JSONObject::toPollOption)
)

private fun JSONObject.toPollOption() = PollOption(
    optionId = optLong("optionId"),
    text = optString("text"),
    voteCount = optInt("voteCount"),
    votedByMe = optBoolean("votedByMe"),
    // 익명 투표면 null 이다 — 빈 배열로 접으면 "아무도 안 골랐다"와 구분되지 않는다
    voterNicknames = optJSONArray("voterNicknames")?.let { array ->
        List(array.length()) { index -> array.optString(index) }
    }
)

private fun JSONObject.toCompanion() = RoomCompanion(
    userId = optLong("userId"),
    nickname = optString("nickname"),
    profileImageUrl = stringOrNull("profileImageUrl"),
    mannerRating = doubleOrNull("mannerRating"),
    mannerScore = intOrNull("mannerScore"),
    oneLineReview = stringOrNull("oneLineReview"),
    reviewed = optBoolean("reviewed")
)

private fun JSONObject.toApplication(): RoomApplication {
    val applicant = optJSONObject("applicant") ?: JSONObject()
    return RoomApplication(
        applicationId = getLong("applicationId"),
        applicationMessage = stringOrNull("applicationMessage"),
        userId = applicant.optLong("userId"),
        nickname = applicant.optString("nickname"),
        profileImageUrl = applicant.stringOrNull("profileImageUrl"),
        gender = applicant.stringOrNull("gender"),
        age = applicant.intOrNull("age"),
        mannerRating = applicant.doubleOrNull("mannerRating"),
        completedTripCount = applicant.optInt("completedTripCount"),
        appliedAt = optString("appliedAt")
    )
}

private fun JSONObject.toKickHistory() = RoomKickHistory(
    kickHistoryId = getLong("kickHistoryId"),
    roomId = getLong("roomId"),
    roomTitle = getString("roomTitle"),
    reason = optString("reason"),
    kickedAt = optString("kickedAt")
)

/**
 * 20-2f 공지 올리기 — 상단 고정은 **최대 1개**다 (`ATTACH-COMPOSER-CANON.md` R5-1).
 *
 * 서버는 고정 개수를 막지 않는다(실서버 방 101 에 2건이 고정돼 있다). 그래서 고정을 켠 채로
 * 올릴 때 **먼저** 기존 고정 공지를 `pinned:false` 로 풀고 새 공지를 만든다.
 * 순서를 뒤집으면 잠깐이지만 고정이 2개가 되고, 실패하면 그대로 남는다.
 */
suspend fun ChatRoomRepository.publishNotice(roomId: Long, notice: String, pinned: Boolean) {
    if (pinned) {
        notices(roomId).pinned.forEach { existing -> updateNotice(roomId, existing.noticeId, pinned = false) }
    }
    createNotice(roomId, notice, pinned)
}
