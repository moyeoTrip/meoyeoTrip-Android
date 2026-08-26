package kr.hanchae.moyeotrip.data.courses

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.doubleOrNull
import kr.hanchae.moyeotrip.data.api.mapArray
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import kr.hanchae.moyeotrip.data.rooms.RoomTag
import org.json.JSONObject

data class TravelCoursePlace(
    val contentId: Long,
    val dayNumber: Int,
    val sequence: Int,
    val visitTime: String?,
    val title: String,
    val thumbnail: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class TravelCourse(
    val courseId: Long,
    val title: String,
    val description: String?,
    val travelTime: String?,
    val distanceKm: Double?,
    val averageRating: Double?,
    val ratingCount: Int,
    val tags: List<RoomTag>,
    val thumbnail: String?,
    val places: List<TravelCoursePlace>,
    val creatorNickname: String? = null,
    /** 코스를 공개한 여행의 시작일. 화면기획 14의 "YYYY.MM.DD 여행 후 공개" 표기에 쓴다. */
    val creatorTravelStartDate: String? = null,
    val chatRoomCount: Int? = null
)

interface TravelCourseRepository {
    suspend fun publicCourses(tagId: Long? = null): List<TravelCourse>

    suspend fun popularCourses(): List<TravelCourse>

    suspend fun tags(): List<RoomTag>

    suspend fun course(courseId: Long): TravelCourse

    suspend fun roomCourse(roomId: Long): TravelCourse
}

class HttpTravelCourseRepository(private val client: MoyeoApiClient) : TravelCourseRepository {
    override suspend fun publicCourses(tagId: Long?): List<TravelCourse> {
        val path = "/api/v1/travel-courses/public" + if (tagId != null) "?tagId=$tagId" else ""
        return client.getArray(path).mapObjects(JSONObject::toCourse)
    }

    override suspend fun popularCourses(): List<TravelCourse> =
        client.getArray("/api/v1/travel-courses/public/popular").mapObjects(JSONObject::toCourse)

    override suspend fun tags(): List<RoomTag> =
        client.getArray("/api/v1/travel-courses/tags").mapObjects { RoomTag(it.getLong("tagId"), it.getString("name")) }

    override suspend fun course(courseId: Long): TravelCourse =
        client.getObject("/api/v1/travel-courses/$courseId").toCourse()

    override suspend fun roomCourse(roomId: Long): TravelCourse =
        client.getObject("/api/v1/travel-courses/chat-rooms/$roomId").getJSONObject("course").toCourse()
}

private fun JSONObject.toCourse() = TravelCourse(
    courseId = getLong("courseId"),
    title = getString("title"),
    description = stringOrNull("description"),
    travelTime = stringOrNull("travelTime"),
    distanceKm = doubleOrNull("distanceKm"),
    averageRating = doubleOrNull("averageRating"),
    ratingCount = optInt("ratingCount"),
    tags = mapArray("tags") { RoomTag(it.getLong("tagId"), it.getString("name")) },
    thumbnail = stringOrNull("thumbnail"),
    places = mapArray("places") { place ->
        TravelCoursePlace(
            contentId = place.getLong("contentId"),
            dayNumber = place.optInt("dayNumber", 1),
            sequence = place.optInt("sequence"),
            visitTime = place.stringOrNull("visitTime"),
            title = place.getString("title"),
            thumbnail = place.stringOrNull("thumbnail"),
            latitude = place.doubleOrNull("latitude"),
            longitude = place.doubleOrNull("longitude")
        )
    }.sortedWith(compareBy({ it.dayNumber }, { it.sequence })),
    creatorNickname = stringOrNull("creatorNickname"),
    creatorTravelStartDate = stringOrNull("creatorTravelStartDate"),
    chatRoomCount = if (has("chatRoomCount") && !isNull("chatRoomCount")) optInt("chatRoomCount") else null
)
