package kr.hanchae.moyeotrip.data.courses

import java.net.URLEncoder
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.doubleOrNull
import kr.hanchae.moyeotrip.data.api.mapArray
import kr.hanchae.moyeotrip.data.api.mapObjects
import kr.hanchae.moyeotrip.data.api.stringOrNull
import kr.hanchae.moyeotrip.data.rooms.ChatRoomSearchResult
import kr.hanchae.moyeotrip.data.rooms.RoomTag
import kr.hanchae.moyeotrip.data.rooms.toChatRoomSearchResult
import org.json.JSONArray
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
    /**
     * `PUBLIC` 또는 `CUSTOM`. 서버는 늘 주는데 예전에는 받지 않았다
     * (2026-09-07 실서버 확인). **18-7 이름 수정은 `CUSTOM` 만 된다** — 이 값이 근거다.
     * 예전 응답을 캐시로 읽는 경우가 있어 기본값을 둔다.
     */
    val type: String? = null,
    val travelTime: String?,
    val distanceKm: Double?,
    val averageRating: Double?,
    val ratingCount: Int,
    val tags: List<RoomTag>,
    val thumbnail: String?,
    val places: List<TravelCoursePlace>,
    val creatorNickname: String? = null,
    /**
     * 코스 작성자 프로필 이미지 (2026-09-02 서버 추가). 14 코스 상세의
     * "○○ 님이 다녀온 코스" 줄 아바타가 이 값을 그린다.
     *
     * 작성자 비공개 코스·탈퇴한 작성자·프로필 이미지가 없는 경우에만 `null` 이고,
     * 그때만 닉네임 동물 아바타로 떨어진다.
     */
    val creatorProfileImageUrl: String? = null,
    /** 코스를 공개한 여행의 시작일. 화면기획 14의 "YYYY.MM.DD 여행 후 공개" 표기에 쓴다. */
    val creatorTravelStartDate: String? = null,
    val chatRoomCount: Int? = null
)

/**
 * 26 마이 `찜한 코스` 목록 (`GET /api/v1/travel-courses/me/favorites`).
 *
 * 응답(`LikedTravelCourseResponse`)은 목록 카드보다 **좁다** — 소요 시간·거리·평점이 없다.
 * [TravelCourse] 로 억지로 채우면 없는 값을 0·null 로 그리게 되므로 따로 둔다.
 */
data class LikedTravelCourse(
    val courseId: Long,
    val title: String,
    val description: String?,
    val thumbnail: String?,
    val tags: List<RoomTag>
)

/** `POST /api/v1/travel-courses/{courseId}/publication` 응답. */
data class CoursePublication(val courseId: Long, val publicationStatus: String)

/**
 * 코스 수정에 실어 보내는 방문지 한 곳.
 *
 * 읽기 모델([TravelCourse] 의 방문지)에는 제목·썸네일·좌표가 더 있지만
 * 서버가 **쓰기에서 받는 것은 이 네 개뿐**이라 전송용을 따로 둔다.
 */
data class CoursePlaceEdit(val contentId: Long, val dayNumber: Int, val sequence: Int, val visitTime: String?)

interface TravelCourseRepository {
    suspend fun publicCourses(tagId: Long? = null): List<TravelCourse>

    suspend fun popularCourses(): List<TravelCourse>

    suspend fun tags(): List<RoomTag>

    suspend fun course(courseId: Long): TravelCourse

    suspend fun roomCourse(roomId: Long): TravelCourse

    /**
     * 18-1 경로 수정 · 18-7 이름·소개 수정 — **같은 엔드포인트**다
     * (`PATCH /travel-courses/chat-rooms/{roomId}`).
     *
     * 2026-09-04 BE 회신 §5 로 `title`·`description` 선택 필드가 붙었다.
     * **생략하면 기존 값을 유지하는 부분 수정**이라 PUT 이 아니라 PATCH 다.
     * 조건: 호스트가 직접 만든 `CUSTOM` 코스이고 **여행 확정 전**일 때만.
     * 제목을 보냈다면 공백일 수 없다(빈 문자열은 400).
     */
    suspend fun updateRoomCourse(
        roomId: Long,
        title: String? = null,
        description: String? = null,
        places: List<CoursePlaceEdit>? = null
    )

    /**
     * 12-1 검색 결과 — GET travel-courses/search?keyword=.
     *
     * 서버는 **제목에 포함되거나 태그명이 일치**하는 공개 코스만 준다. 소개글(`description`)은
     * 검색 대상이 아니다 — 실서버에서 `청송`(소개글에만 있는 낱말)이 0건인 이유다.
     * 클라이언트가 결과를 다시 거르거나 채우지 않는다.
     */
    suspend fun searchCourses(keyword: String): List<TravelCourse>

    /**
     * 14 코스 상세 `모집 중인 모임 보기` — GET travel-courses/{courseId}/chat-rooms.
     *
     * 서버 설명 그대로 "모집 마감 전이고 로그인 사용자가 아직 참가하지 않은 방"만 온다.
     * 응답은 11 탐색 카드와 같은 `SearchChatRoomResponse` 다.
     */
    suspend fun courseChatRooms(courseId: Long): List<ChatRoomSearchResult>

    /**
     * 27-4 코스 평가 — `POST /api/v1/travel-courses/chat-rooms/{roomId}/rating` `{ score: 1~5 }`.
     *
     * 서버 조건: **완료한 여행의 참가자만** 평가할 수 있다(그 외에는 400 40006).
     * 이 호출이 14 코스 상세의 `averageRating`/`ratingCount` 를 만드는 유일한 자리다.
     */
    suspend fun rateRoomCourse(roomId: Long, score: Int)

    /**
     * 26 마이 `찜한 코스` — `GET /api/v1/travel-courses/me/favorites`.
     *
     * 오래 "조회 API 가 없다"고 적어 두고 탭 자체를 빼 두었는데, 실서버는 200 을 준다.
     * 모임 찜(`chat-rooms/my/favorites`)과 **다른 목록**이다 — 코스는 코스고 모집은 모집이다.
     */
    suspend fun likedCourses(): List<LikedTravelCourse>

    /**
     * 14 코스 상세의 하트 — `POST /api/v1/travel-courses/{courseId}/favorite` (토글).
     * 모임 찜은 이미 서버에 저장하는데 코스 찜만 화면 안에서만 켜졌다 꺼졌다 했다.
     * 응답의 `favorite` 가 새 상태다.
     */
    suspend fun toggleCourseFavorite(courseId: Long): Boolean

    /**
     * 27-3 코스 공개 — `POST /api/v1/travel-courses/{courseId}/publication`.
     *
     * **되돌릴 수 없다.** 호출부는 두 단계 확인을 지난 뒤에만 부른다.
     * 이 호출이 없어서 `공개할게요` 가 화면 상태만 바꾸고 서버를 부르지 않았다.
     */
    suspend fun publishCourse(
        courseId: Long,
        title: String,
        description: String,
        showCreatorNickname: Boolean
    ): CoursePublication
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

    // null 은 **키를 아예 넣지 않는다**는 뜻이다 — 서버가 "생략" 으로 읽어 기존 값을 유지한다.
    // JSONObject.NULL 을 넣으면 지우려는 시도로 읽힐 수 있다.
    override suspend fun updateRoomCourse(
        roomId: Long,
        title: String?,
        description: String?,
        places: List<CoursePlaceEdit>?
    ) {
        val body = JSONObject()
        title?.let { body.put("title", it) }
        description?.let { body.put("description", it) }
        places?.let { list ->
            body.put(
                "places",
                JSONArray().apply {
                    list.forEach { place ->
                        put(
                            JSONObject()
                                .put("contentId", place.contentId)
                                .put("dayNumber", place.dayNumber)
                                .put("sequence", place.sequence)
                                .put("visitTime", place.visitTime ?: JSONObject.NULL)
                        )
                    }
                }
            )
        }
        client.send("PATCH", "/api/v1/travel-courses/chat-rooms/$roomId", body)
    }

    override suspend fun searchCourses(keyword: String): List<TravelCourse> {
        // 검색어는 한글이 대부분이라 반드시 인코딩해서 보낸다.
        val encoded = URLEncoder.encode(keyword, "UTF-8")
        return client.getArray("/api/v1/travel-courses/search?keyword=$encoded").mapObjects(JSONObject::toCourse)
    }

    override suspend fun courseChatRooms(courseId: Long): List<ChatRoomSearchResult> =
        client.getArray("/api/v1/travel-courses/$courseId/chat-rooms").mapObjects(JSONObject::toChatRoomSearchResult)

    override suspend fun likedCourses(): List<LikedTravelCourse> =
        client.getArray("/api/v1/travel-courses/me/favorites").mapObjects { course ->
            LikedTravelCourse(
                courseId = course.getLong("courseId"),
                title = course.getString("title"),
                description = course.stringOrNull("description"),
                thumbnail = course.stringOrNull("thumbnail"),
                tags = course.mapArray("tags") { RoomTag(it.getLong("tagId"), it.getString("name")) }
            )
        }

    override suspend fun toggleCourseFavorite(courseId: Long): Boolean =
        client.sendForObject("POST", "/api/v1/travel-courses/$courseId/favorite").optBoolean("favorite")

    override suspend fun publishCourse(
        courseId: Long,
        title: String,
        description: String,
        showCreatorNickname: Boolean
    ): CoursePublication {
        val json = client.sendForObject(
            "POST",
            "/api/v1/travel-courses/$courseId/publication",
            JSONObject()
                .put("title", title)
                .put("description", description)
                .put("showCreatorNickname", showCreatorNickname)
        )
        return CoursePublication(
            courseId = json.optLong("courseId", courseId),
            publicationStatus = json.optString("publicationStatus")
        )
    }

    override suspend fun rateRoomCourse(roomId: Long, score: Int) {
        client.send(
            "POST",
            "/api/v1/travel-courses/chat-rooms/$roomId/rating",
            JSONObject().put("score", score)
        )
    }
}

private fun JSONObject.toCourse() = TravelCourse(
    courseId = getLong("courseId"),
    title = getString("title"),
    description = stringOrNull("description"),
    type = stringOrNull("type"),
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
    creatorProfileImageUrl = stringOrNull("creatorProfileImageUrl"),
    creatorTravelStartDate = stringOrNull("creatorTravelStartDate"),
    chatRoomCount = if (has("chatRoomCount") && !isNull("chatRoomCount")) optInt("chatRoomCount") else null
)
