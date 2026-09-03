package kr.hanchae.moyeotrip.data.social

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.doubleOrNull
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import org.json.JSONObject

data class SocialUser(val userId: Long, val nickname: String, val profileImageUrl: String?, val introduction: String?)

data class Friend(val friendshipId: Long, val user: SocialUser, val lastActive: String?)

data class FriendRequest(val requestId: Long, val user: SocialUser, val requestedAt: String)

data class BlockedUser(val userId: Long, val nickname: String, val profileImageUrl: String?, val blockedAt: String)

/** 도감의 한 여행 기록. `oneLineReview` 는 "내가 그 사람에게 남긴" 메시지다(27-1). */
data class DexMemory(val chatRoomId: Long, val tripTitle: String, val tripDate: String, val oneLineReview: String?)

data class DexCompanion(
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val mannerRating: Double?,
    val tripCount: Int,
    val latestTripDate: String,
    val latestTripTitle: String,
    /** 프로필 카드 뒷면에서 쓴다. 응답에 이미 들어 있어 추가 호출이 필요 없다. */
    val memories: List<DexMemory> = emptyList()
)

interface SocialRepository {
    suspend fun friends(): List<Friend>

    suspend fun receivedRequests(): List<FriendRequest>

    suspend fun sentRequests(): List<FriendRequest>

    suspend fun acceptRequest(requestId: Long)

    suspend fun rejectRequest(requestId: Long)

    suspend fun cancelRequest(requestId: Long)

    suspend fun sendRequest(userId: Long)

    suspend fun removeFriend(userId: Long)

    suspend fun blocks(): List<BlockedUser>

    suspend fun block(userId: Long)

    suspend fun unblock(userId: Long)

    suspend fun travelDex(): List<DexCompanion>
}

class HttpSocialRepository(private val client: MoyeoApiClient) : SocialRepository {
    override suspend fun friends(): List<Friend> =
        client.getObject("/api/v1/users/me/friends").getJSONArray("friends").mapObjects { friend ->
            Friend(
                friendshipId = friend.getLong("friendshipId"),
                user = friend.getJSONObject("user").toSocialUser(),
                lastActive = friend.stringOrNull("lastActive")
            )
        }

    override suspend fun receivedRequests(): List<FriendRequest> = requests("/api/v1/users/me/friend-requests/received")

    override suspend fun sentRequests(): List<FriendRequest> = requests("/api/v1/users/me/friend-requests/sent")

    override suspend fun acceptRequest(requestId: Long) {
        client.send("POST", "/api/v1/users/me/friend-requests/$requestId/accept")
    }

    override suspend fun rejectRequest(requestId: Long) {
        client.send("POST", "/api/v1/users/me/friend-requests/$requestId/reject")
    }

    override suspend fun cancelRequest(requestId: Long) {
        client.send("DELETE", "/api/v1/users/me/friend-requests/$requestId")
    }

    /** 친구 신청 보내기 — 경로 변수는 requestId 가 아니라 상대 userId 다. */
    override suspend fun sendRequest(userId: Long) {
        client.send("POST", "/api/v1/users/me/friend-requests/$userId")
    }

    /** 친구 삭제 — 경로 변수는 friendshipId 가 아니라 상대 userId 다(friendshipId 를 넣으면 404). */
    override suspend fun removeFriend(userId: Long) {
        client.send("DELETE", "/api/v1/users/me/friends/$userId")
    }

    override suspend fun blocks(): List<BlockedUser> =
        client.getArray("/api/v1/users/me/blocks").mapObjects { blocked ->
            BlockedUser(
                userId = blocked.getLong("userId"),
                nickname = blocked.getString("nickname"),
                profileImageUrl = blocked.stringOrNull("profileImageUrl"),
                blockedAt = blocked.optString("blockedAt")
            )
        }

    override suspend fun block(userId: Long) {
        client.send("POST", "/api/v1/users/me/blocks/$userId")
    }

    override suspend fun unblock(userId: Long) {
        client.send("DELETE", "/api/v1/users/me/blocks/$userId")
    }

    override suspend fun travelDex(): List<DexCompanion> =
        client.getObject("/api/v1/users/me/travel-dex").getJSONArray("companions").mapObjects { companion ->
            DexCompanion(
                userId = companion.getLong("userId"),
                nickname = companion.getString("nickname"),
                profileImageUrl = companion.stringOrNull("profileImageUrl"),
                mannerRating = companion.doubleOrNull("mannerRating"),
                tripCount = companion.optInt("tripCount"),
                latestTripDate = companion.optString("latestTripDate"),
                latestTripTitle = companion.optString("latestTripTitle"),
                memories = companion.optJSONArray("memories")?.mapObjects { memory ->
                    DexMemory(
                        chatRoomId = memory.optLong("chatRoomId"),
                        tripTitle = memory.optString("tripTitle"),
                        tripDate = memory.optString("tripDate"),
                        oneLineReview = memory.stringOrNull("oneLineReview")
                    )
                }.orEmpty()
            )
        }

    private suspend fun requests(path: String): List<FriendRequest> =
        client.getObject(path).getJSONArray("requests").mapObjects { request ->
            FriendRequest(
                requestId = request.getLong("requestId"),
                user = request.getJSONObject("user").toSocialUser(),
                requestedAt = request.optString("requestedAt")
            )
        }
}

private fun JSONObject.toSocialUser() = SocialUser(
    userId = getLong("userId"),
    nickname = optString("nickname"),
    profileImageUrl = stringOrNull("profileImageUrl"),
    introduction = stringOrNull("introduction")
)
