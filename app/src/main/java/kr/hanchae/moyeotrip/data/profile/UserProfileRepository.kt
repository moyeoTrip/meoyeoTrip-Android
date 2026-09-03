package kr.hanchae.moyeotrip.data.profile

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.intOrNull
import kr.hanchae.moyeotrip.data.api.mapArray
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import org.json.JSONArray
import org.json.JSONObject

data class ProfileOption(val id: Long, val label: String)

data class ProfileOptions(val travelStyles: List<ProfileOption>, val interestedRegions: List<ProfileOption>)

data class ServerUserProfile(
    val nickname: String,
    val profileImageUrl: String?,
    val introduction: String?,
    val travelStyles: List<ProfileOption>,
    val interestedRegions: List<ProfileOption>,
    val birthDate: String?,
    val gender: String,
    val chatNotificationMode: String,
    val recruitmentDeadlineEnabled: Boolean,
    val socialActivityEnabled: Boolean,
    val marketingEnabled: Boolean
)

/**
 * 다른 사용자의 공개 프로필. GET /api/v1/users/{userId}/profile
 *
 * `nicknameColor` 는 이 응답과 받은 평가에서만 내려온다 —
 * 도감·멤버·동행자 목록 응답에는 없어서 목록에서는 색을 쓸 수 없다.
 * 전체 여행/호스트/피드 횟수는 어떤 응답에도 없다.
 */
data class ServerPublicProfile(
    val userId: Long,
    val nickname: String,
    val nicknameColor: String?,
    val profileImageUrl: String?,
    val introduction: String?,
    val travelStyles: List<ProfileOption>,
    val interestedRegions: List<ProfileOption>,
    val mannerRating: Double?,
    /**
     * 완료한 여행 수 · 공개 피드 수 (2026-08-26 추가).
     *
     * 호스트 횟수는 **기획에 없다** — 서버도 주지 않고 화면에도 칸을 만들지 않는다.
     */
    val completedTripCount: Int?,
    val feedCount: Int?
)

/**
 * 다른 여행자가 그 사용자에게 남긴 평가. GET /api/v1/users/{userId}/travel-reviews
 *
 * 응답에 여행 제목과 작성 시각이 없다 — 어느 여행에서 받은 평가인지, 최신순인지 알 수 없다.
 */
data class ServerReceivedTravelReview(
    val reviewerId: Long,
    val reviewerNickname: String,
    val reviewerNicknameColor: String?,
    val reviewerProfileImageUrl: String?,
    val content: String
)

data class ProfileUpdate(
    val introduction: String?,
    val travelStyleIds: List<Long>,
    val interestedRegionIds: List<Long>,
    val birthDate: String,
    val gender: String
)

interface UserProfileRepository {
    suspend fun profile(): ServerUserProfile

    suspend fun options(): ProfileOptions

    suspend fun updateProfile(update: ProfileUpdate): ServerUserProfile

    suspend fun publicProfile(userId: Long): ServerPublicProfile

    suspend fun receivedTravelReviews(userId: Long): List<ServerReceivedTravelReview>
}

class HttpUserProfileRepository(private val client: MoyeoApiClient) : UserProfileRepository {
    override suspend fun profile(): ServerUserProfile = client.getObject("/api/v1/users/me/profile").toProfile()

    override suspend fun options(): ProfileOptions {
        val json = client.getObject("/api/v1/users/me/profile/options")
        return ProfileOptions(
            travelStyles = json.mapArray("travelStyles", JSONObject::toStyleOption),
            interestedRegions = json.mapArray("interestedRegions", JSONObject::toRegionOption)
        )
    }

    override suspend fun updateProfile(update: ProfileUpdate): ServerUserProfile {
        val body = JSONObject()
            .put("introduction", update.introduction)
            .put("travelStyleIds", JSONArray(update.travelStyleIds))
            .put("interestedRegionIds", JSONArray(update.interestedRegionIds))
            .put("birthDate", update.birthDate)
            .put("gender", update.gender)
        return client.sendForObject("PUT", "/api/v1/users/me/profile", body).toProfile()
    }

    override suspend fun publicProfile(userId: Long): ServerPublicProfile =
        client.getObject("/api/v1/users/$userId/profile").toPublicProfile()

    override suspend fun receivedTravelReviews(userId: Long): List<ServerReceivedTravelReview> =
        client.getArray("/api/v1/users/$userId/travel-reviews").mapObjects(JSONObject::toReceivedReview)
}

private fun JSONObject.toStyleOption() = ProfileOption(getLong("id"), getString("label"))

private fun JSONObject.toPublicProfile() = ServerPublicProfile(
    userId = optLong("userId"),
    nickname = optString("nickname"),
    nicknameColor = stringOrNull("nicknameColor"),
    profileImageUrl = stringOrNull("profileImageUrl"),
    introduction = stringOrNull("introduction"),
    travelStyles = mapArray("travelStyles", JSONObject::toStyleOption),
    interestedRegions = mapArray("interestedRegions", JSONObject::toRegionOption),
    mannerRating = if (isNull("mannerRating")) null else optDouble("mannerRating"),
    completedTripCount = intOrNull("completedTripCount"),
    feedCount = intOrNull("feedCount")
)

private fun JSONObject.toReceivedReview() = ServerReceivedTravelReview(
    reviewerId = optLong("reviewerId"),
    reviewerNickname = optString("reviewerNickname"),
    reviewerNicknameColor = stringOrNull("reviewerNicknameColor"),
    reviewerProfileImageUrl = stringOrNull("reviewerProfileImageUrl"),
    content = optString("content")
)

/** 관심 지역 후보의 라벨 키는 signguName 이다 (GET users/me/profile/options 실응답 기준). */
private fun JSONObject.toRegionOption() = ProfileOption(
    getLong("id"),
    stringOrNull("signguName") ?: optString("label")
)

private fun JSONObject.toProfile() = ServerUserProfile(
    nickname = optString("nickname"),
    profileImageUrl = stringOrNull("profileImageUrl"),
    introduction = stringOrNull("introduction"),
    travelStyles = mapArray("travelStyles", JSONObject::toStyleOption),
    interestedRegions = mapArray("interestedRegions", JSONObject::toRegionOption),
    birthDate = stringOrNull("birthDate"),
    gender = optString("gender"),
    chatNotificationMode = optString("chatNotificationMode"),
    recruitmentDeadlineEnabled = optBoolean("recruitmentDeadlineEnabled"),
    socialActivityEnabled = optBoolean("socialActivityEnabled"),
    marketingEnabled = optBoolean("marketingEnabled")
)
