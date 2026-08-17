package kr.hanchae.moyeotrip.ui.navigation

object AppRoutes {
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val MEETINGS = "meetings"
    const val MEETINGS_APPLIED = "meetings_applied"
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
    const val CUSTOM_COURSE = "custom_course/{draftId}"
    const val CREATE_SCHEDULE = "create_schedule/{draftId}"
    const val CREATE_PEOPLE = "create_people/{draftId}"
    const val CREATE_MEET_POINT = "create_meet_point/{draftId}"
    const val CREATE_SUMMARY = "create_summary/{draftId}"
    const val COURSE_ROUTE = "course_route/{tripId}"
    const val NOTICE_HISTORY = "notice_history/{tripId}"
    const val HOST_MANAGE = "host_manage/{tripId}"
    const val FEED_WRITE = "feed_write"
    const val SEARCH = "search"
    const val MOCK_AUTH = "mock_auth"
    const val TRIP_CONFIRMED = "trip_confirmed"
    const val CHAT_MENU = "chat_menu/{threadId}"
    const val CHAT_ATTACH = "chat_attach"
    const val FRIENDS = "friends"
    const val TRIP_MESSAGE = "trip_message"
    const val REPORT = "report"
    const val BLOCKED_USERS = "blocked_users"
    const val COURSE_PUBLISH = "course_publish"
    const val TRIP_DAY = "trip_day/{threadId}"
    const val NOTIFICATION_DETAIL = "notification_detail"
    const val ACCOUNT_DELETE = "account_delete"
    const val SYSTEM_MAINTENANCE = "system_maintenance"
    const val SYSTEM_ERROR = "system_error"
    const val FEED_COMMENTS = "feed_comments/{postId}"

    fun courseDetail(courseId: String) = "course/$courseId"

    fun tripDetail(tripId: String) = "trip/$tripId"

    fun feedDetail(postId: String) = "feed/$postId"

    fun chatRoom(threadId: String) = "chat/$threadId"

    fun chatMenu(threadId: String) = "chat_menu/$threadId"

    fun tripDay(threadId: String) = "trip_day/$threadId"

    fun feedComments(postId: String) = "feed_comments/$postId"

    fun createRecruitment(courseId: String) = "create_recruitment/$courseId"

    fun customCourse(draftId: String) = "custom_course/$draftId"

    fun createSchedule(draftId: String) = "create_schedule/$draftId"

    fun createPeople(draftId: String) = "create_people/$draftId"

    fun createMeetPoint(draftId: String) = "create_meet_point/$draftId"

    fun createSummary(draftId: String) = "create_summary/$draftId"

    fun courseRoute(tripId: String) = "course_route/$tripId"

    fun noticeHistory(tripId: String) = "notice_history/$tripId"

    fun hostManage(tripId: String) = "host_manage/$tripId"
}

enum class BottomTab(val route: String, val label: String) {
    Home(AppRoutes.HOME, "홈"),
    Explore(AppRoutes.EXPLORE, "탐색"),
    Meetings(AppRoutes.MEETINGS, "모임"),
    Feed(AppRoutes.FEED, "피드"),
    My(AppRoutes.MY, "마이")
}
