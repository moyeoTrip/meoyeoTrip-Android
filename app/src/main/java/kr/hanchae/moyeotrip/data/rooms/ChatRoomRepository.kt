package kr.hanchae.moyeotrip.data.rooms

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.doubleOrNull
import kr.hanchae.moyeotrip.data.api.intOrNull
import kr.hanchae.moyeotrip.data.api.longOrNull
import kr.hanchae.moyeotrip.data.api.mapArray
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import org.json.JSONArray
import org.json.JSONObject

data class RoomTag(val tagId: Long, val name: String)

data class ChatRoomSearchResult(
    val roomId: Long,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val tripType: String,
    val startDate: String,
    val endDate: String?,
    val recruitmentDeadlineDate: String?,
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
    val hostProfileImageUrl: String?,
    val participantCount: Int,
    val maxParticipants: Int,
    val status: String,
    val favorite: Boolean,
    val participantImageUrls: List<String?>
)

enum class RoomApplicationResult { JOINED, WAITLISTED, PENDING_APPROVAL }

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

data class RoomMessage(
    val messageId: Long,
    val type: String,
    val senderId: Long?,
    val senderNickname: String,
    val content: String,
    val createdAt: String,
    val imageUrl: String?
)

data class RoomMessagePage(val messages: List<RoomMessage>, val nextId: Long?, val hasNext: Boolean)

interface ChatRoomRepository {
    suspend fun search(keyword: String? = null, limit: Int? = null): List<ChatRoomSearchResult>

    suspend fun room(roomId: Long): ChatRoomDetail

    suspend fun canApply(roomId: Long): Boolean

    suspend fun apply(roomId: Long, message: String?): RoomApplicationResult

    suspend fun cancelApplication(roomId: Long)

    suspend fun toggleFavorite(roomId: Long): Boolean

    suspend fun myRooms(): List<MyChatRoom>

    suspend fun myWaitingRooms(): List<MyWaitingRoom>

    suspend fun myKickHistories(): List<RoomKickHistory>

    suspend fun members(roomId: Long): RoomMembers

    suspend fun notices(roomId: Long): RoomNotices

    suspend fun currentRoadmap(roomId: Long): RoomRoadmap

    suspend fun messages(roomId: Long, beforeMessageId: Long? = null, limit: Int? = null): RoomMessagePage

    suspend fun sendMessage(roomId: Long, content: String): RoomMessage
}

class HttpChatRoomRepository(private val client: MoyeoApiClient) : ChatRoomRepository {
    override suspend fun search(keyword: String?, limit: Int?): List<ChatRoomSearchResult> {
        val query = buildList {
            keyword?.takeIf(String::isNotBlank)?.let { add("keyword=${java.net.URLEncoder.encode(it, "UTF-8")}") }
            limit?.let { add("limit=$it") }
        }.joinToString("&")
        val path = "/api/v1/chat-rooms/search" + if (query.isBlank()) "" else "?$query"
        return client.getArray(path).mapObjects(JSONObject::toSearchResult)
    }

    override suspend fun room(roomId: Long): ChatRoomDetail =
        client.getObject("/api/v1/chat-rooms/$roomId").toRoomDetail()

    override suspend fun canApply(roomId: Long): Boolean =
        client.getObject("/api/v1/chat-rooms/$roomId/join-eligibility").optBoolean("canApply")

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
}

private fun JSONObject.toSearchResult() = ChatRoomSearchResult(
    roomId = getLong("roomId"),
    title = getString("title"),
    description = stringOrNull("description"),
    thumbnail = stringOrNull("thumbnail"),
    tripType = optString("tripType"),
    startDate = optString("startDate"),
    endDate = stringOrNull("endDate"),
    recruitmentDeadlineDate = stringOrNull("recruitmentDeadlineDate"),
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
    hostProfileImageUrl = stringOrNull("hostProfileImageUrl"),
    participantCount = optInt("participantCount"),
    maxParticipants = optInt("maxParticipants"),
    status = optString("status"),
    favorite = optBoolean("favorite"),
    participantImageUrls = mapArray("participants") { it.stringOrNull("profileImageUrl") }
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
    imageUrl = stringOrNull("imageUrl")
)

private fun JSONObject.toKickHistory() = RoomKickHistory(
    kickHistoryId = getLong("kickHistoryId"),
    roomId = getLong("roomId"),
    roomTitle = getString("roomTitle"),
    reason = optString("reason"),
    kickedAt = optString("kickedAt")
)
