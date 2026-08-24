package kr.hanchae.moyeotrip.data.feed

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
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

data class FeedLikeResult(val liked: Boolean, val likeCount: Int)

enum class FeedTab { FRIENDS, DISCOVER }

interface FeedRepository {
    suspend fun feeds(tab: FeedTab, limit: Int = 30): FeedsPage

    suspend fun feed(feedId: Long): ServerFeed

    suspend fun comments(feedId: Long): List<FeedComment>

    suspend fun addComment(feedId: Long, content: String, parentCommentId: Long? = null): FeedComment

    suspend fun toggleLike(feedId: Long): FeedLikeResult
}

class HttpFeedRepository(private val client: MoyeoApiClient) : FeedRepository {
    override suspend fun feeds(tab: FeedTab, limit: Int): FeedsPage {
        val json = client.getObject("/api/v1/feeds?tab=${tab.name}&limit=$limit")
        return FeedsPage(
            feeds = json.mapArray("feeds", JSONObject::toFeed),
            nextId = json.longOrNull("nextId")
        )
    }

    override suspend fun feed(feedId: Long): ServerFeed = client.getObject("/api/v1/feeds/$feedId").toFeed()

    override suspend fun comments(feedId: Long): List<FeedComment> =
        client.getArray("/api/v1/feeds/$feedId/comments").mapObjects(JSONObject::toComment)

    override suspend fun addComment(feedId: Long, content: String, parentCommentId: Long?): FeedComment {
        val body = JSONObject().put("content", content)
        parentCommentId?.let { body.put("parentCommentId", it) }
        return client.sendForObject("POST", "/api/v1/feeds/$feedId/comments", body).toComment()
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
