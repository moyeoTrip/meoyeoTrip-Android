package kr.hanchae.moyeotrip.data.profile

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.mapArray
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
}

private fun JSONObject.toStyleOption() = ProfileOption(getLong("id"), getString("label"))

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
