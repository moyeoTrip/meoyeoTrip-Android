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

    /**
     * 21 특수 메시지 카드 6종. `threadId` 는 어느 방의 특수 메시지를 보여줄지다 —
     * 지정하면 그 방만 읽고, 없으면 내 모임 중 특수 메시지가 있는 방을 훑는다.
     */
    const val SPECIAL_MESSAGES = "special_messages?threadId={threadId}"
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
    const val CUSTOM_COURSE = "custom_course/{draftId}?courseId={courseId}"
    const val CREATE_SCHEDULE = "create_schedule/{draftId}"

    /**
     * 17-4 Step 3 인원. `capacity` 는 최대 인원을 미리 정해 두고 여는 캡처용 인자다
     * (17-4a 4명 · 17-4b 10명 — 인원수별 멘트 변형). 0 이면 초안 기본값을 그대로 쓴다.
     */
    const val CREATE_PEOPLE = "create_people/{draftId}?capacity={capacity}"
    const val CREATE_MEET_POINT = "create_meet_point/{draftId}"
    const val CREATE_DETAIL = "create_detail/{draftId}"
    const val CREATE_SUMMARY = "create_summary/{draftId}"
    const val COURSE_ROUTE = "course_route/{tripId}"
    const val NOTICE_HISTORY = "notice_history/{tripId}"
    const val HOST_MANAGE = "host_manage/{tripId}"

    // 24-1~24-5. roomId 는 캡처가 특정 여행을 지정할 때만 붙는다 — 없으면 첫 후보를 고른다.
    const val FEED_WRITE = "feed_write?step={step}&roomId={roomId}"
    const val SEARCH = "search"
    const val MOCK_AUTH = "mock_auth"
    const val MOCK_AUTH_STEP = "mock_auth/{startStep}"

    /** 20-4 여행 확정 모먼트 — 확정된 방을 지정할 수 있다. 없으면 내 모임에서 찾는다. */
    const val TRIP_CONFIRMED = "trip_confirmed?tripId={tripId}"
    const val CHAT_MENU = "chat_menu/{threadId}"

    // 20-2 첨부 시트. threadId 는 어느 방에 공유하는지다 — 캡처 라우트(`chatattach`)는 인자 없이 들어와
    // 배경 채팅방이 빈 상태로 깔린다.
    const val CHAT_ATTACH = "chat_attach?threadId={threadId}"

    // 20-2a~20-2f 첨부 작성 화면 6종 (ATTACH-COMPOSER-CANON). 20-2 타일에서 각자의 화면으로 온다.
    // threadId 가 없으면(캡처 라우트) 화면은 그대로 열리고 보내기만 잠긴다.
    const val ATTACH_PHOTO = "attach_photo?threadId={threadId}"
    const val ATTACH_PLACE = "attach_place?threadId={threadId}"
    const val ATTACH_MAP = "attach_map?threadId={threadId}"
    const val ATTACH_POLL = "attach_poll?threadId={threadId}"
    const val ATTACH_SETTLEMENT = "attach_settlement?threadId={threadId}"
    const val ATTACH_NOTICE = "attach_notice?threadId={threadId}"

    // 12-1 검색 결과. 12 검색에서 검색어를 확정하면 여기로 온다.
    const val SEARCH_RESULTS = "search_results?keyword={keyword}"

    // ATTACH-COMPOSER-CANON §6 — 버튼은 있는데 이어지는 화면이 없던 자리들.
    // 방을 지정하지 않고 들어오면(캡처 라우트) 화면은 열리되 실행 버튼만 잠긴다.

    /** 27-4 코스 평가 — POST travel-courses/chat-rooms/{roomId}/rating */
    const val COURSE_RATING = "course_rating?threadId={threadId}"

    /** 18-1 여행 확정 / 불발 (호스트) — POST chat-rooms/{id}/status */
    const val TRIP_STATUS = "trip_status?tripId={tripId}"

    /** 18-2 집합 정보 수정 (호스트) — PUT chat-rooms/{id}/meeting-info */
    const val MEETING_EDIT = "meeting_edit?tripId={tripId}"

    /** 20-1c 이 모임 알림 — GET/PUT notifications/settings/chat-rooms/{roomId} */
    const val ROOM_NOTIFICATION = "room_notif?threadId={threadId}"

    /** 20-3a 공지 수정 · 삭제 — PUT/DELETE chat-rooms/{id}/notices/{noticeId} */
    const val NOTICE_EDIT = "notice_edit?tripId={tripId}&noticeId={noticeId}"

    // 내가 만든 것을 되돌리는 화면들 (2026-09-04 BE 회신으로 API 가 열렸다).
    // 정본 `docs/api/BACKEND_REQUEST_CHANGES_2026-09-04.md`.
    // 프로필 이미지 삭제는 **없다** — 회신 §7 대로 API 가 없고 기획에도 없다.
    const val FEED_ACTIONS = "feed_actions?feedId={feedId}"
    const val FEED_DELETE = "feed_delete?feedId={feedId}"
    const val FEED_EDIT = "feed_edit?feedId={feedId}"
    const val COMMENT_EDIT = "comment_edit?feedId={feedId}"
    const val RECRUIT_EDIT = "recruit_edit?tripId={tripId}"
    const val COURSE_TITLE_EDIT = "course_title_edit?tripId={tripId}"
    const val MESSAGE_DELETE = "message_delete?tripId={tripId}"

    /** 26-1 찜한 모집 — GET chat-rooms/my/favorites */
    const val FAVORITE_ROOMS = "favorite_rooms"

    /** 19-2 참가 신청 취소 확인 — DELETE chat-rooms/{id}/applications/me */
    const val APPLY_CANCEL = "apply_cancel?tripId={tripId}"

    /** 29-1a 차단 해제 확인 — DELETE users/me/blocks/{userId} */
    const val UNBLOCK_CONFIRM = "unblock_confirm?userId={userId}&nickname={nickname}"

    /** 27-2a 친구 정리 (친구 끊기) — DELETE users/me/friends/{friendUserId} */
    const val FRIEND_MANAGE = "friend_manage?userId={userId}&nickname={nickname}&subtitle={subtitle}"

    /** 13-2 내 강퇴 이력 — GET chat-rooms/my-kick-histories */
    const val KICK_HISTORY = "kick_history"

    /** 29-5 계정 연결 (로그인 방식) — GET/POST auth/providers */
    const val ACCOUNT_PROVIDERS = "account_providers"
    const val FRIENDS = "friends"
    const val TRIP_MESSAGE = "trip_message"

    /**
     * 30-2 피드 신고 — 전체 화면이 아니라 피드 상세 위로 올라오는 바텀시트다.
     *
     * **피드 전용이다**(정본 `docs/alignment/REPORT-CANON.md`). 서버가 접수하는 신고는
     * 피드뿐이라(`POST /feeds/{feedId}/reports`) 대상은 방 id 가 아니라 **피드 id** 다.
     * 멤버·채팅방·댓글 신고는 접수 API 가 없어 누른 자리에서 안내 다이얼로그를 띄운다.
     */
    const val REPORT = "report?feedId={feedId}"
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
    const val QA_LEAVE = "qa_leave?threadId={threadId}"

    // 31-1 참가자 나가기 — 31 을 역할로 가른 쪽(정본 §6-5). 화면을 새로 만들지 않는다.
    const val QA_LEAVE_MEMBER = "qa_leave_member?threadId={threadId}"
    const val QA_APPLY = "qa_apply/{tripId}"

    // 28-1 캡처용 — 프로필 수정을 여행 취향 편집 시트가 열린 채로 연다
    const val QA_PROFILE_TASTE_EDIT = "qa_profile_taste_edit"

    // 20-1a 캡처용 — 채팅 메뉴를 멤버 액션 시트가 열린 채로 연다 (changeLog14)
    const val QA_MEMBER_ACTIONS = "qa_member_actions/{threadId}"

    // 20-1b 캡처용 — 채팅 메뉴를 내보내기 사유 시트가 열린 채로 연다 (changeLog14)
    const val QA_MEMBER_REMOVE = "qa_member_remove/{threadId}"

    fun feedWrite(step: Int = 1, roomId: Long? = null) =
        if (roomId == null) "feed_write?step=$step" else "feed_write?step=$step&roomId=$roomId"

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

    /** 21 특수 메시지 — 방을 지정하지 않으면 내 모임을 훑는다. */
    fun specialMessages(threadId: String? = null) =
        if (threadId.isNullOrBlank()) "special_messages" else "special_messages?threadId=$threadId"

    fun chatAttach(threadId: String? = null) =
        if (threadId.isNullOrBlank()) "chat_attach" else "chat_attach?threadId=$threadId"

    /** 30-2 피드 신고 시트. 피드를 안 넘기면 신고할 대상이 없어 시트가 안내만 한다. */
    fun report(feedId: Long? = null) = if (feedId == null) "report" else "report?feedId=$feedId"

    /** 31 나가기(호스트) · 31-1 나가기(참가자). 둘 다 배경에 채팅방을 깐다. */
    fun qaLeave(threadId: String? = null) = if (threadId.isNullOrBlank()) "qa_leave" else "qa_leave?threadId=$threadId"

    fun qaLeaveMember(threadId: String? = null) =
        if (threadId.isNullOrBlank()) "qa_leave_member" else "qa_leave_member?threadId=$threadId"

    /** 20-2a~20-2f 공통 — 방이 없으면 인자 없이 열린다(캡처 라우트). */
    private fun attachRoute(base: String, threadId: String?) =
        if (threadId.isNullOrBlank()) base else "$base?threadId=$threadId"

    fun attachPhoto(threadId: String? = null) = attachRoute("attach_photo", threadId)

    fun attachPlace(threadId: String? = null) = attachRoute("attach_place", threadId)

    fun attachMap(threadId: String? = null) = attachRoute("attach_map", threadId)

    fun attachPoll(threadId: String? = null) = attachRoute("attach_poll", threadId)

    fun attachSettlement(threadId: String? = null) = attachRoute("attach_settlement", threadId)

    fun attachNotice(threadId: String? = null) = attachRoute("attach_notice", threadId)

    fun searchResults(keyword: String) = "search_results?keyword=" + java.net.URLEncoder.encode(keyword, "UTF-8")

    private fun encoded(value: String) = java.net.URLEncoder.encode(value, "UTF-8")

    /** 27-4 · 20-1c 는 방을 인자로 받는다. 없으면(캡처) 화면만 열린다. */
    fun courseRating(threadId: String? = null) = attachRoute("course_rating", threadId)

    fun roomNotification(threadId: String? = null) = attachRoute("room_notif", threadId)

    fun tripStatus(tripId: String? = null) = if (tripId.isNullOrBlank()) "trip_status" else "trip_status?tripId=$tripId"

    fun meetingEdit(tripId: String? = null) =
        if (tripId.isNullOrBlank()) "meeting_edit" else "meeting_edit?tripId=$tripId"

    fun noticeEdit(tripId: String, noticeId: Long) = "notice_edit?tripId=$tripId&noticeId=$noticeId"

    fun feedActions(feedId: String) = "feed_actions?feedId=$feedId"

    fun feedDelete(feedId: String) = "feed_delete?feedId=$feedId"

    fun feedEdit(feedId: String) = "feed_edit?feedId=$feedId"

    fun commentEdit(feedId: String) = "comment_edit?feedId=$feedId"

    fun recruitEdit(tripId: String) = "recruit_edit?tripId=$tripId"

    fun courseTitleEdit(tripId: String) = "course_title_edit?tripId=$tripId"

    fun messageDelete(tripId: String) = "message_delete?tripId=$tripId"

    fun tripConfirmed(tripId: String? = null) =
        if (tripId.isNullOrBlank()) "trip_confirmed" else "trip_confirmed?tripId=$tripId"

    fun applyCancel(tripId: String? = null) =
        if (tripId.isNullOrBlank()) "apply_cancel" else "apply_cancel?tripId=$tripId"

    fun unblockConfirm(userId: Long, nickname: String) = "unblock_confirm?userId=$userId&nickname=${encoded(nickname)}"

    fun friendManage(userId: Long, nickname: String, subtitle: String) =
        "friend_manage?userId=$userId&nickname=${encoded(nickname)}&subtitle=${encoded(subtitle)}"

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

    /**
     * 17-1 코스 직접 만들기. [courseId] 를 주면 **그 등록 코스를 불러온 상태**로 연다
     * (딥링크·QA 진입 · `create_people` 의 `capacity` 와 같은 관례).
     */
    fun customCourse(draftId: String, courseId: Long? = null) = if (courseId == null) {
        "custom_course/$draftId"
    } else {
        "custom_course/$draftId?courseId=$courseId"
    }

    fun createSchedule(draftId: String) = "create_schedule/$draftId"

    fun createPeople(draftId: String, capacity: Int? = null) = if (capacity == null) {
        "create_people/$draftId"
    } else {
        "create_people/$draftId?capacity=$capacity"
    }

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
