package kr.hanchae.moyeotrip.ui.navigation

object AppRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val MEETINGS = "meetings"
    const val FEED = "feed"
    const val MY = "my"
    const val CHAT_LIST = "chat_list"
    const val COURSE_DETAIL = "course/{courseId}"
    const val TRIP_DETAIL = "trip/{tripId}"
    const val FEED_DETAIL = "feed/{postId}"
    const val CHAT_ROOM = "chat/{threadId}"
    const val SPECIAL_MESSAGES = "special_messages"
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile_edit"
    const val MY_FEED = "my_feed"
    const val FRIEND_DEX = "friend_dex"
    const val SETTINGS = "settings"
    const val CUSTOMER_CENTER = "customer_center"
    const val NOTIFICATIONS = "notifications"
    const val CREATE_RECRUITMENT = "create_recruitment/{courseId}"
    const val HOST_MANAGE = "host_manage/{tripId}"
    const val FEED_WRITE = "feed_write"
    const val SEARCH = "search"
    const val MOCK_AUTH = "mock_auth"

    fun courseDetail(courseId: String) = "course/$courseId"

    fun tripDetail(tripId: String) = "trip/$tripId"

    fun feedDetail(postId: String) = "feed/$postId"

    fun chatRoom(threadId: String) = "chat/$threadId"

    fun createRecruitment(courseId: String) = "create_recruitment/$courseId"

    fun hostManage(tripId: String) = "host_manage/$tripId"
}

enum class BottomTab(val route: String, val label: String) {
    Home(AppRoutes.HOME, "홈"),
    Explore(AppRoutes.EXPLORE, "탐색"),
    Meetings(AppRoutes.MEETINGS, "모임"),
    Feed(AppRoutes.FEED, "피드"),
    My(AppRoutes.MY, "마이")
}
