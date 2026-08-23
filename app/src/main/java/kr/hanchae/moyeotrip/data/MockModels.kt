package kr.hanchae.moyeotrip.data

data class TripCourse(
    val id: String,
    val title: String,
    val region: String,
    val oneLine: String,
    val imageEmoji: String,
    val duration: String,
    val courseTime: String,
    val distance: String,
    val price: String,
    val host: String,
    val hostAvatar: String,
    val participants: Int,
    val capacity: Int,
    val minParticipants: Int = 3,
    val deadlineLabel: String,
    val startLabel: String,
    val meetingPoint: String,
    val rating: Double,
    val tags: List<String>,
    val stops: List<String>,
    val recruitmentNote: String,
    val publisher: CoursePublisher? = null
)

data class CoursePublisher(
    val name: String,
    val avatar: String,
    val publishedAfterTrip: String,
    val recruitmentCount: Int
)

enum class CourseSource(val label: String) {
    Linked("등록된 코스"),
    Custom("호스트 직접 코스")
}

enum class TripScheduleType(val label: String) {
    DayTrip("당일치기"),
    Overnight("1박 이상")
}

data class RouteStop(val id: String, val day: Int = 1, val time: String, val name: String, val memo: String)

data class MeetingLocation(
    val name: String,
    val detail: String,
    val latitude: Double,
    val longitude: Double,
    val meetingTime: String
)

data class TripRecruitment(
    val id: String,
    val courseId: String,
    val title: String,
    val recruitmentName: String = title,
    val scheduleDate: String,
    val scheduleTime: String,
    /**
     * 마이(26) 여행 카드에서 날짜 뒤에 붙는 집합 시간. 화면기획은 모집마다 표기 여부가 다르므로
     * (경주 14:00 / 포항 09:30만 표기) `scheduleTime` 에서 기계적으로 뽑지 않고 값으로 둔다.
     */
    val assemblyTimeLabel: String? = null,
    val meetingPoint: String,
    val joined: Int,
    val capacity: Int,
    val minParticipants: Int,
    val ddayLabel: String,
    val statusLabel: String,
    val host: String,
    val hostAvatar: String,
    val chatThreadId: String,
    val courseSource: CourseSource = CourseSource.Linked,
    val scheduleType: TripScheduleType = TripScheduleType.DayTrip,
    val endDate: String? = null,
    val recruitmentDeadline: String = "",
    val estimatedCostPerPerson: Int = 45_000,
    // 나이대 기본값은 4개 플랫폼 공통으로 25~35세다
    val minimumAge: Int = 25,
    val maximumAge: Int = 35,
    val genderCondition: String = "성별 무관",
    val meetingLocation: MeetingLocation = MeetingLocation(
        name = meetingPoint,
        detail = "정문 앞",
        latitude = 36.435612,
        longitude = 129.057214,
        meetingTime = scheduleTime.substringBefore(" ").substringBefore("-").trim()
    ),
    val routeStops: List<RouteStop> = emptyList()
)

enum class TripApplicationStatus(val label: String) {
    PendingApproval("승인 대기"),
    Waitlisted("대기열")
}

data class TripApplication(
    val id: String,
    val tripId: String,
    val status: TripApplicationStatus,
    val waitlistPosition: Int? = null
)

data class RecruitmentNotice(
    val id: String,
    val tripId: String,
    val title: String,
    val body: String,
    val author: String,
    val createdAt: String,
    val isPinned: Boolean,
    val includesMap: Boolean = false
)

data class RecruitmentDraft(
    val id: String,
    val selectedCourseId: String,
    val courseSource: CourseSource = CourseSource.Linked,
    val scheduleType: TripScheduleType = TripScheduleType.DayTrip,
    val travelDate: String,
    val startTime: String = "08:00",
    val endTime: String = "18:00",
    val endDate: String? = null,
    val recruitmentDeadline: String = "2026.05.22 (목) 23:59",
    val recruitmentName: String,
    val estimatedCostPerPerson: Int = 45_000,
    val minimumAge: Int = 25,
    val maximumAge: Int = 35,
    val genderCondition: String = "성별 무관",
    val meetingLocation: MeetingLocation,
    val routeStops: List<RouteStop>,
    val capacity: Int,
    val minParticipants: Int,
    val note: String
)

data class ChatThread(
    val id: String,
    val title: String,
    val partner: String,
    val avatar: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val countText: String,
    val statusText: String,
    val stateLabel: String,
    val messages: List<ChatMessage>,
    val isReadOnly: Boolean = false,
    val tripId: String? = null,
    /** 행 부제로 보여줄 여행 이름 (화면기획 19: 모집 이름 아래 여행 제목) */
    val courseLine: String = "",
    val closureReason: String? = null,
    val archiveNotice: String? = null,
    val archiveStatus: String? = null
)

data class ChatMessage(val sender: String, val text: String, val time: String, val mine: Boolean = false)

enum class FeedVisibility(val label: String) {
    Public("전체공개"),
    Friends("친구만"),
    Private("나만 보기")
}

enum class DogamVisibility(val label: String) {
    Public("전체공개"),
    Friends("친구에게만"),
    Private("나만 보기")
}

data class DogamFriend(
    val id: String,
    val nickname: String,
    val avatar: String,
    val lastMetAt: String,
    val metCount: Int
)

data class FeedPost(
    val id: String,
    val author: String,
    val avatar: String,
    val region: String,
    val imageEmoji: String,
    val title: String,
    val body: String,
    val routeSummary: String,
    val visibility: FeedVisibility,
    val likes: Int,
    val comments: Int,
    val photoCountText: String = "1/10"
)

data class Profile(
    val name: String,
    // 비공개 닉네임(가입 때 고른 동물 캐릭터 이름). 공개 표시명(name)과 다르다 — 화면기획 28.
    val nickname: String = "",
    val animalBuddy: String,
    val region: String,
    val bio: String,
    // 공개 프로필(화면기획 25)의 소개 카드 본문. 마이 요약(26)의 한 줄 bio와 다르다.
    val intro: String = "",
    val badges: List<String>,
    val joinedTrips: Int,
    val hostedTrips: Int,
    val feedCount: Int
)
