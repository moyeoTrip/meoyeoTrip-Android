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

    /** 25-1 · 프로필 카드 뒷면. 캡처 전용 진입이고, 실사용에서는 25 에서 뒤집는다. */
    const val PROFILE_CARD_BACK = "profile_card_back"
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
    const val CREATE_DETAIL = "create_detail/{draftId}"
    const val CREATE_SUMMARY = "create_summary/{draftId}"
    const val COURSE_ROUTE = "course_route/{tripId}"
    const val NOTICE_HISTORY = "notice_history/{tripId}"
    const val HOST_MANAGE = "host_manage/{tripId}"
    const val FEED_WRITE = "feed_write?step={step}"
    const val SEARCH = "search"
    const val MOCK_AUTH = "mock_auth"
    const val MOCK_AUTH_STEP = "mock_auth/{startStep}"
    const val TRIP_CONFIRMED = "trip_confirmed"
    const val CHAT_MENU = "chat_menu/{threadId}"

    // 20-2 첨부 시트. threadId 는 어느 방에 공유하는지다 — 캡처 라우트(`chatattach`)는 인자 없이 들어와
    // 기존 목데이터 배경을 그대로 쓴다.
    const val CHAT_ATTACH = "chat_attach?threadId={threadId}"
    const val FRIENDS = "friends"
    const val TRIP_MESSAGE = "trip_message"
    const val REPORT = "report"
    const val BLOCKED_USERS = "blocked_users"
    const val COURSE_PUBLISH = "course_publish"
    const val TRIP_DAY = "trip_day/{threadId}"
    const val NOTIFICATION_DETAIL = "notification_detail"

    // 29-4 / 29-4a 오픈소스 라이선스 (changeLog17) — 앱에 내장한 정적 데이터라 로그인·서버와 무관하다
    const val OSS_LICENSES = "oss_licenses"
    const val OSS_LICENSE_DETAIL = "oss_license_detail/{slug}"

    // 13-1 내보내기 안내 — 강퇴 알림 탭에서만 진입한다 (changeLog14)
    // notificationId 가 있으면 서버 강퇴 이력(GET notifications/{id}/kick-history)을 보여준다
    const val REMOVAL_REASON = "removal_reason?notificationId={notificationId}"
    const val ACCOUNT_DELETE = "account_delete"
    const val SYSTEM_MAINTENANCE = "system_maintenance"
    const val SYSTEM_ERROR = "system_error"
    const val FEED_COMMENTS = "feed_comments/{postId}"
    const val PLACE_SEARCH = "place_search/{draftId}"
    const val PLACE_DETAIL = "place_detail/{draftId}/{contentId}"
    const val TERMS_DETAIL = "terms_detail/{document}/{source}"
    const val QA_SPLASH = "qa_splash"
    const val QA_DESIGN_SYSTEM = "qa_design_system"
    const val QA_STATES = "qa_states"
    const val QA_LEAVE = "qa_leave"
    const val QA_APPLY = "qa_apply/{tripId}"

    // 28-1 캡처용 — 프로필 수정을 여행 취향 편집 시트가 열린 채로 연다
    const val QA_PROFILE_TASTE_EDIT = "qa_profile_taste_edit"

    // 20-1a 캡처용 — 채팅 메뉴를 멤버 액션 시트가 열린 채로 연다 (changeLog14)
    const val QA_MEMBER_ACTIONS = "qa_member_actions/{threadId}"

    // 20-1b 캡처용 — 채팅 메뉴를 내보내기 사유 시트가 열린 채로 연다 (changeLog14)
    const val QA_MEMBER_REMOVE = "qa_member_remove/{threadId}"

    fun feedWrite(step: Int = 1) = "feed_write?step=$step"

    fun removalReason(notificationId: Long? = null) = if (notificationId != null) {
        "removal_reason?notificationId=$notificationId"
    } else {
        "removal_reason"
    }

    fun ossLicenseDetail(slug: String) = "oss_license_detail/$slug"

    fun courseDetail(courseId: String) = "course/$courseId"

    fun tripDetail(tripId: String) = "trip/$tripId"

    fun feedDetail(postId: String) = "feed/$postId"

    fun chatRoom(threadId: String) = "chat/$threadId"

    fun chatMenu(threadId: String) = "chat_menu/$threadId"

    fun chatAttach(threadId: String? = null) =
        if (threadId.isNullOrBlank()) "chat_attach" else "chat_attach?threadId=$threadId"

    fun tripDay(threadId: String) = "trip_day/$threadId"

    fun feedComments(postId: String) = "feed_comments/$postId"

    fun placeSearch(draftId: String) = "place_search/$draftId"

    fun placeDetail(draftId: String, contentId: String) = "place_detail/$draftId/$contentId"

    fun termsDetail(document: String, source: String) = "terms_detail/$document/$source"

    fun qaApply(tripId: String) = "qa_apply/$tripId"

    fun qaMemberActions(threadId: String) = "qa_member_actions/$threadId"

    fun qaMemberRemove(threadId: String) = "qa_member_remove/$threadId"

    fun mockAuth(startStep: String) = "mock_auth/$startStep"

    fun createRecruitment(courseId: String) = "create_recruitment/$courseId"

    fun customCourse(draftId: String) = "custom_course/$draftId"

    fun createSchedule(draftId: String) = "create_schedule/$draftId"

    fun createPeople(draftId: String) = "create_people/$draftId"

    fun createMeetPoint(draftId: String) = "create_meet_point/$draftId"

    fun createDetail(draftId: String) = "create_detail/$draftId"

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
