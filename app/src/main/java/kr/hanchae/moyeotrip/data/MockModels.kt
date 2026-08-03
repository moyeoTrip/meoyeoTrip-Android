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
    val recommendedSeason: String,
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
    val recruitmentNote: String
)

data class TripRecruitment(
    val id: String,
    val courseId: String,
    val title: String,
    val scheduleDate: String,
    val scheduleTime: String,
    val meetingPoint: String,
    val joined: Int,
    val capacity: Int,
    val minParticipants: Int,
    val ddayLabel: String,
    val statusLabel: String,
    val host: String,
    val hostAvatar: String,
    val chatThreadId: String
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
    val animalBuddy: String,
    val region: String,
    val bio: String,
    val badges: List<String>,
    val joinedTrips: Int,
    val hostedTrips: Int,
    val feedCount: Int
)
