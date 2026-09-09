package kr.hanchae.moyeotrip.data

enum class CourseSource(val label: String) {
    Linked("등록된 코스"),
    Custom("호스트 직접 코스")
}

enum class TripScheduleType(val label: String) {
    DayTrip("당일치기"),
    Overnight("1박 이상")
}

/**
 * 모집 만들기(17-x)에서 사용자가 담는 방문지 한 곳.
 *
 * 좌표는 방문지 검색(TourAPI)이 준 값을 그대로 들고 온다 — 코스 미리보기를 실제 지도에 그리려면
 * 이름만으로는 부족하다. 좌표를 모르는 방문지는 null 로 두고 지도에 올리지 않는다.
 */
data class RouteStop(
    val id: String,
    val day: Int = 1,
    val time: String,
    val name: String,
    val memo: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class MeetingLocation(
    val name: String,
    val detail: String,
    val latitude: Double,
    val longitude: Double,
    val meetingTime: String
)

/**
 * 모집 만들기 5단계가 채워 가는 초안. 서버에 대응 API 가 없어 앱 세션 안에만 산다
 * ([RecruitmentDraftStore]). 마지막 단계에서 POST chat-rooms 한 번으로 넘어간다.
 */
data class RecruitmentDraft(
    val id: String,
    val courseSource: CourseSource = CourseSource.Linked,
    val scheduleType: TripScheduleType = TripScheduleType.DayTrip,
    /**
     * 여행 날짜와 모집 마감일의 **폼 기본값**이다.
     *
     * 예전에는 둘 다 비워 뒀다. 그런데 17-6 요약에서 「마감」 값이 통째로 비어
     * 사용자가 「마감 일시가 안 보인다」고 지적했다 (2026-09-09).
     * 웹은 이미 날짜 기본값을 갖고 있었으므로 세 구현을 같은 규칙으로 맞춘다:
     * **출발 = 2주 뒤 · 마감 = 출발 3일 전**. 호스트가 2단계에서 언제든 바꾼다.
     */
    val travelDate: String = defaultTravelDate(),
    val startTime: String = "08:00",
    val endTime: String = "18:00",
    val endDate: String? = null,
    val recruitmentDeadline: String = defaultRecruitmentDeadline(),
    val recruitmentName: String = "",
    val estimatedCostPerPerson: Int = 0,
    // 나이대 기본값은 4개 플랫폼 공통으로 25~35세다
    val minimumAge: Int = 25,
    val maximumAge: Int = 35,
    val genderCondition: String = "성별 무관",
    val meetingLocation: MeetingLocation = MeetingLocation("", "", 0.0, 0.0, "08:00"),
    val routeStops: List<RouteStop> = emptyList(),
    /**
     * 17-1 커스텀 코스가 몇 날로 나뉘어 있는지. "+ 다음 날 추가" 가 늘리고, 방문지는
     * 마지막 날([RouteStop.day])에 담긴다. 방문지가 없는 날도 있을 수 있어
     * `routeStops` 의 최댓값으로는 알 수 없다 — 그래서 따로 든다.
     */
    val dayCount: Int = 1,
    // 최소 3명은 정책값이다 (낯선 사람과 단둘이 되는 일을 막는다)
    // 최대 인원 기본값 5 는 4개 플랫폼 공통이다 — 여기만 6 이라 17-4 가 다르게 찍혔다.
    val capacity: Int = 5,
    val minParticipants: Int = 3,
    val note: String = "",
    /**
     * 서버 공개 코스(GET travel-courses/public)를 고른 경우에만 채워진다.
     * POST chat-rooms 가 `courseType=PUBLIC` + `courseId` 를 요구하기 때문이다.
     */
    val serverCourseId: Long? = null,
    val serverCourseTitle: String? = null,
    /** 17-5 신청 승인 방식. 서버 `joinApprovalMode`(AUTO·MANUAL)로 그대로 전달된다. */
    val autoApproval: Boolean = true
)

enum class FeedVisibility(val label: String) {
    Public("전체공개"),
    Friends("친구만"),
    Private("나만 보기")
}

/** 「2026.09.23 (수)」 — 오늘부터 2주 뒤. 모집 만들기 2단계의 시작값이다. */
internal fun defaultTravelDate(): String = draftDateText(days = 14)

/** 「2026.09.20 (일) 23:59」 — 출발 3일 전 자정 직전. */
internal fun defaultRecruitmentDeadline(): String = "${draftDateText(days = 11)} 23:59"

private fun draftDateText(days: Int): String = java.time.LocalDate.now()
    .plusDays(days.toLong())
    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd (E)", java.util.Locale.KOREAN))
