package kr.hanchae.moyeotrip.data.feed

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.MultipartFile
import kr.hanchae.moyeotrip.data.api.longOrNull
import kr.hanchae.moyeotrip.data.api.mapArray
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import org.json.JSONObject

data class FeedAuthor(val userId: Long, val nickname: String, val profileImageUrl: String?)

data class FeedTripInfo(
    val chatRoomId: Long?,
    val courseId: Long?,
    val courseTitle: String?,
    val startDate: String?,
    val endDate: String?,
    val placeTitles: List<String>
)

data class ServerFeed(
    val feedId: Long,
    val author: FeedAuthor,
    val content: String,
    val visibility: String,
    /**
     * 신고 누적(서로 다른 이용자 3건)으로 비공개 처리된 피드.
     *
     * 작성자가 자기 피드를 볼 때만 `true` 로 온다. 작성자가 직접 고른 `PRIVATE` 는 이 값이 `false` 라
     * 둘을 구분할 수 있다 — 화면은 이 값으로만 "신고되어 비공개 처리되었습니다" 안내를 띄운다.
     */
    val hiddenByReports: Boolean,
    val imageUrls: List<String>,
    val trip: FeedTripInfo?,
    val likeCount: Int,
    val commentCount: Int,
    val liked: Boolean,
    val createdAt: String
)

data class FeedsPage(val feeds: List<ServerFeed>, val nextId: Long?)

data class FeedComment(
    val commentId: Long,
    val author: FeedAuthor,
    val content: String,
    val createdAt: String,
    val replies: List<FeedComment>
)

/**
 * GET /api/v1/feeds/{feedId}/comments 한 묶음.
 *
 * 2026-09-02 서버 변경으로 응답이 **배열에서 객체로** 바뀌었다 —
 * `{ "comments": [...], "nextId": … }` 다. 최상위 댓글은 최신 ID 부터 오고,
 * 다음 묶음이 있으면 마지막 최상위 댓글 ID 가 [nextId], 마지막 묶음이면 `null` 이다.
 * 대댓글은 각 최상위 댓글의 `replies` 에 그대로 들어 있어 페이지네이션 대상이 아니다.
 */
data class FeedCommentsPage(val comments: List<FeedComment>, val nextId: Long?)

data class FeedLikeResult(val liked: Boolean, val likeCount: Int)

enum class FeedTab { FRIENDS, DISCOVER }

interface FeedRepository {
    suspend fun feeds(tab: FeedTab, limit: Int = 30): FeedsPage

    /**
     * 24 피드 작성 — POST /api/v1/feeds (multipart).
     * 완료된 여행 채팅방 하나에 묶이고 사진이 **최소 한 장** 있어야 서버가 받는다.
     */
    suspend fun createFeed(
        chatRoomId: Long,
        content: String,
        visibility: String,
        images: List<MultipartFile>
    ): ServerFeed

    suspend fun feed(feedId: Long): ServerFeed

    /**
     * 23·23-1 댓글 목록. [beforeCommentId] 는 **첫 요청에서 생략**하고, 다음 묶음부터
     * 직전 응답의 `nextId` 를 그대로 넘긴다(그 ID 보다 작은 최상위 댓글이 온다).
     * [limit] 은 최상위 댓글 수이고 서버가 1~50 으로 보정한다.
     */
    suspend fun comments(feedId: Long, beforeCommentId: Long? = null, limit: Int = 20): FeedCommentsPage

    suspend fun addComment(feedId: Long, content: String, parentCommentId: Long? = null): FeedComment

    suspend fun toggleLike(feedId: Long): FeedLikeResult

    /** 신고 사유 목록. 코드와 표시 문구를 서버가 함께 준다 — 클라가 문구를 갖지 않는다. */
    suspend fun reportReasons(): List<FeedReportReason>

    /**
     * 피드 신고. 성공은 204 다.
     *
     * [details] 는 선택이고 서버 상한이 300자다. 서로 다른 이용자의 신고 3건이 모이면
     * 서버가 `visibility=PRIVATE` · `hiddenByReports=true` 로 바꾼다.
     * 오류: 본인 피드 400 · 권한 없음 403 · 없는 피드 404 · 중복 신고 409 `40917`(실서버 확인).
     */
    suspend fun reportFeed(feedId: Long, reason: String, details: String? = null)
}

/** GET /api/v1/feeds/report-reasons 항목. */
data class FeedReportReason(val reason: String, val displayName: String)

class HttpFeedRepository(private val client: MoyeoApiClient) : FeedRepository {
    override suspend fun feeds(tab: FeedTab, limit: Int): FeedsPage {
        val json = client.getObject("/api/v1/feeds?tab=${tab.name}&limit=$limit")
        return FeedsPage(
            feeds = json.mapArray("feeds", JSONObject::toFeed),
            nextId = json.longOrNull("nextId")
        )
    }

    override suspend fun createFeed(
        chatRoomId: Long,
        content: String,
        visibility: String,
        images: List<MultipartFile>
    ): ServerFeed = client.sendMultipartJsonAndFilesForObject(
        method = "POST",
        path = "/api/v1/feeds",
        jsonPartName = "request",
        jsonPart = JSONObject()
            .put("chatRoomId", chatRoomId)
            .put("content", content)
            .put("visibility", visibility),
        files = images
    ).toFeed()

    override suspend fun feed(feedId: Long): ServerFeed = client.getObject("/api/v1/feeds/$feedId").toFeed()

    override suspend fun comments(feedId: Long, beforeCommentId: Long?, limit: Int): FeedCommentsPage {
        val cursor = beforeCommentId?.let { "&beforeCommentId=$it" }.orEmpty()
        val json = client.getObject("/api/v1/feeds/$feedId/comments?limit=$limit$cursor")
        return FeedCommentsPage(
            comments = json.mapArray("comments", JSONObject::toComment),
            nextId = json.longOrNull("nextId")
        )
    }

    override suspend fun addComment(feedId: Long, content: String, parentCommentId: Long?): FeedComment {
        val body = JSONObject().put("content", content)
        parentCommentId?.let { body.put("parentCommentId", it) }
        return client.sendForObject("POST", "/api/v1/feeds/$feedId/comments", body).toComment()
    }

    override suspend fun reportReasons(): List<FeedReportReason> =
        client.getArray("/api/v1/feeds/report-reasons").mapObjects {
            FeedReportReason(it.optString("reason"), it.optString("displayName"))
        }

    override suspend fun reportFeed(feedId: Long, reason: String, details: String?) {
        val body = JSONObject().put("reason", reason)
        details?.trim()?.takeIf(String::isNotEmpty)?.let { body.put("details", it.take(300)) }
        client.send("POST", "/api/v1/feeds/$feedId/reports", body)
    }

    override suspend fun toggleLike(feedId: Long): FeedLikeResult {
        val json = client.sendForObject("POST", "/api/v1/feeds/$feedId/like")
        return FeedLikeResult(liked = json.optBoolean("liked"), likeCount = json.optInt("likeCount"))
    }
}

private fun JSONObject.toAuthor() = FeedAuthor(
    userId = optLong("userId"),
    nickname = optString("nickname"),
    profileImageUrl = stringOrNull("profileImageUrl")
)

private fun JSONObject.toFeed() = ServerFeed(
    feedId = getLong("feedId"),
    author = getJSONObject("author").toAuthor(),
    content = optString("content"),
    visibility = optString("visibility"),
    hiddenByReports = optBoolean("hiddenByReports"),
    imageUrls = mapArray("images") { it.stringOrNull("imageUrl") }.filterNotNull(),
    trip = optJSONObject("trip")?.let { trip ->
        FeedTripInfo(
            chatRoomId = trip.longOrNull("chatRoomId"),
            courseId = trip.longOrNull("courseId"),
            courseTitle = trip.stringOrNull("courseTitle"),
            startDate = trip.stringOrNull("startDate"),
            endDate = trip.stringOrNull("endDate"),
            placeTitles = trip.mapArray("places") { it.stringOrNull("title") }.filterNotNull()
        )
    },
    likeCount = optInt("likeCount"),
    commentCount = optInt("commentCount"),
    liked = optBoolean("liked"),
    createdAt = optString("createdAt")
)

private fun JSONObject.toComment(): FeedComment = FeedComment(
    commentId = getLong("commentId"),
    author = getJSONObject("author").toAuthor(),
    content = optString("content"),
    createdAt = optString("createdAt"),
    replies = mapArray("replies") { it.toComment() }
)
