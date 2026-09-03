package kr.hanchae.moyeotrip.ui.navigation

import kr.hanchae.moyeotrip.data.network.OfflineExperience
import kr.hanchae.moyeotrip.data.oss.OssLicenseCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QaStartRequestTest {
    @Test
    fun parsesDirectLaunchScreensWithExplicitIds() {
        assertEquals(
            AppRoutes.feedDetail("feed-3"),
            QaStartRequest.parse("feed-detail:feed-3").toRoute()
        )
        assertEquals(
            AppRoutes.courseDetail("course-andong-hahoe"),
            QaStartRequest.parse("course-detail:course-andong-hahoe").toRoute()
        )
        assertEquals(
            AppRoutes.tripDetail("trip-ulleung-island"),
            QaStartRequest.parse("trip-detail:trip-ulleung-island").toRoute()
        )
        assertEquals(
            AppRoutes.chatRoom("chat-cheongsong-juwangsan"),
            QaStartRequest.parse("chat:chat-cheongsong-juwangsan").toRoute()
        )
    }

    /** 대상을 지정하지 않은 진입은 빈 대상으로 연다 — 목데이터 식별자를 기본값으로 끼워 넣지 않는다. */
    @Test
    fun screensWithoutExplicitIdsOpenWithNoTarget() {
        assertEquals(AppRoutes.feedDetail(""), QaStartRequest.parse("feed-detail").toRoute())
        assertEquals(AppRoutes.courseDetail(""), QaStartRequest.parse("course").toRoute())
        assertEquals(AppRoutes.HOME, QaStartRequest.parse(null).toRoute())
    }

    @Test
    fun opensOssLicenseScreensWithHyphenStrippedCaptureKeys() {
        assertEquals(AppRoutes.OSS_LICENSES, QaStartRequest.parse("oss-licenses").toRoute())
        assertEquals(AppRoutes.OSS_LICENSES, QaStartRequest.parse("osslicenses").toRoute())
        assertEquals(
            AppRoutes.ossLicenseDetail("sentry-android"),
            QaStartRequest.parse("oss-license-detail:sentry-android").toRoute()
        )
        // 인자가 없으면 목록의 첫 항목을 보여준다
        assertEquals(
            AppRoutes.ossLicenseDetail(OssLicenseCatalog.items.first().slug),
            QaStartRequest.parse("oss-license-detail").toRoute()
        )
    }

    @Test
    fun opensMyHubSupportScreensBySharedQaNames() {
        assertEquals(AppRoutes.MY_FEED, QaStartRequest.parse("my-feed").toRoute())
        assertEquals(AppRoutes.MY_FEED, QaStartRequest.parse("myFeed").toRoute())
        assertEquals(AppRoutes.CUSTOMER_CENTER, QaStartRequest.parse("customer-center").toRoute())
        assertEquals(AppRoutes.CUSTOMER_CENTER, QaStartRequest.parse("customerCenter").toRoute())
    }

    @Test
    fun treatsExploreMapSpellingVariantsAsMapMode() {
        assertTrue(QaStartRequest.parse("explore-map").startsInExploreMap)
        assertTrue(QaStartRequest.parse("exploreMap").startsInExploreMap)
        assertEquals(AppRoutes.EXPLORE, QaStartRequest.parse("map").toRoute())
    }

    @Test
    fun opensEveryRecruitmentChangeLogQaStateDirectly() {
        assertEquals(AppRoutes.createRecruitment(""), QaStartRequest.parse("create").toRoute())
        assertEquals(AppRoutes.customCourse("new"), QaStartRequest.parse("customCourse").toRoute())
        assertEquals(AppRoutes.createSchedule("new"), QaStartRequest.parse("createSchedule").toRoute())
        assertEquals(AppRoutes.createMeetPoint("new"), QaStartRequest.parse("createMeet").toRoute())
        assertEquals(AppRoutes.createSummary("new"), QaStartRequest.parse("createSummary").toRoute())
        assertEquals(AppRoutes.courseRoute("room-21"), QaStartRequest.parse("courseEdit:room-21").toRoute())
        assertEquals(AppRoutes.noticeHistory("room-21"), QaStartRequest.parse("noticeHistory:room-21").toRoute())
        assertEquals(AppRoutes.MEETINGS_APPLIED, QaStartRequest.parse("chatListApplied").toRoute())
    }

    @Test
    fun opensChangeLogSixAndSevenCaptureStatesDirectly() {
        val draftId = "new"
        assertEquals(AppRoutes.placeSearch(draftId), QaStartRequest.parse("place-search").toRoute())
        assertEquals(
            AppRoutes.placeDetail(draftId, "2299341"),
            QaStartRequest.parse("place-detail").toRoute()
        )
        assertEquals(AppRoutes.createDetail(draftId), QaStartRequest.parse("create-detail").toRoute())
        assertEquals(AppRoutes.createPeople(draftId), QaStartRequest.parse("create-people").toRoute())
        // 17-4a/17-4b 는 같은 화면의 인원수별 멘트 변형이다 — 최대 인원만 다르게 열린다
        assertEquals(
            AppRoutes.createPeople(draftId, capacity = 4),
            QaStartRequest.parse("create-people-small").toRoute()
        )
        assertEquals(
            AppRoutes.createPeople(draftId, capacity = 10),
            QaStartRequest.parse("create-people-large").toRoute()
        )
        assertEquals(AppRoutes.createSummary(draftId), QaStartRequest.parse("create-summary-linked").toRoute())
        assertEquals(AppRoutes.createSummary(draftId), QaStartRequest.parse("create-summary-custom").toRoute())
        assertEquals(AppRoutes.termsDetail("service", "signup"), QaStartRequest.parse("terms-detail").toRoute())
        assertEquals(AppRoutes.termsDetail("privacy", "signup"), QaStartRequest.parse("terms-privacy").toRoute())
        assertEquals(AppRoutes.termsDetail("location", "signup"), QaStartRequest.parse("terms-location").toRoute())
        assertEquals(AppRoutes.termsDetail("marketing", "signup"), QaStartRequest.parse("terms-marketing").toRoute())
        assertEquals(AppRoutes.termsDetail("service", "settings"), QaStartRequest.parse("terms-settings").toRoute())
        assertEquals(AppRoutes.mockAuth("email"), QaStartRequest.parse("email-auth").toRoute())
        assertEquals(AppRoutes.qaApply(""), QaStartRequest.parse("apply").toRoute())
        assertEquals(AppRoutes.qaLeave(), QaStartRequest.parse("leave").toRoute())
        assertEquals("qa_leave", QaStartRequest.parse("leave").toRoute())
        // 방을 넘기면 배경에 그 채팅방이 깔린다 (안 넘기면 "참여 중인 모임이 없어요" 로 남았다).
        assertEquals("qa_leave?threadId=room-22", QaStartRequest.parse("leave:room-22").toRoute())
    }

    @Test
    fun opensOnboardingAndAuthSubstepsDirectly() {
        assertEquals(AppRoutes.mockAuth("onb-1"), QaStartRequest.parse("onb-1").toRoute())
        assertEquals(AppRoutes.mockAuth("onb-2"), QaStartRequest.parse("onb-2").toRoute())
        assertEquals(AppRoutes.mockAuth("onb-3"), QaStartRequest.parse("onb-3").toRoute())
        assertEquals(AppRoutes.mockAuth("login"), QaStartRequest.parse("login").toRoute())
        assertEquals(AppRoutes.mockAuth("nickname"), QaStartRequest.parse("nickname").toRoute())
        assertEquals(AppRoutes.mockAuth("profile-basic"), QaStartRequest.parse("profile-basic").toRoute())
        assertEquals(AppRoutes.mockAuth("profile-image"), QaStartRequest.parse("profile-image").toRoute())
        assertEquals(AppRoutes.mockAuth("terms"), QaStartRequest.parse("terms").toRoute())
    }

    @Test
    fun opensOfflineAndTripConfirmationQaStatesDirectly() {
        assertEquals(AppRoutes.HOME, QaStartRequest.parse("offline").toRoute())
        assertEquals(
            OfflineExperience.NoCache,
            QaStartRequest.parse("offline").offlineExperienceOverride
        )
        assertEquals(AppRoutes.HOME, QaStartRequest.parse("offlineCached").toRoute())
        assertEquals(
            OfflineExperience.Cached,
            QaStartRequest.parse("offlineCached").offlineExperienceOverride
        )
        assertEquals(AppRoutes.chatRoom(""), QaStartRequest.parse("offlineChat").toRoute())
        assertEquals(
            OfflineExperience.Cached,
            QaStartRequest.parse("offlineChat").offlineExperienceOverride
        )
        // 20-4 는 확정된 방을 인자로 받는다. 인자가 없으면 내 모임에서 찾는다.
        assertEquals(AppRoutes.tripConfirmed(), QaStartRequest.parse("tripConfirmed").toRoute())
        assertEquals(
            AppRoutes.tripConfirmed("room-101"),
            QaStartRequest.parse("tripConfirmed:room-101").toRoute()
        )
    }

    @Test
    fun opensChangeLogManagementSafetyAndSystemScreensDirectly() {
        val chatId = "chat-cheongsong-juwangsan"
        assertEquals(AppRoutes.chatMenu(chatId), QaStartRequest.parse("chatMenu:$chatId").toRoute())
        // 캡처 라우트는 방 인자 없이 목데이터 배경으로 열린다 — 20-2 시트에 threadId 가 붙어도 그대로다
        assertEquals(AppRoutes.chatAttach(), QaStartRequest.parse("chatAttach").toRoute())
        assertEquals("chat_attach?threadId=room-22", QaStartRequest.parse("chatAttach:room-22").toRoute())
        assertEquals(AppRoutes.FRIENDS, QaStartRequest.parse("friends").toRoute())
        assertEquals(AppRoutes.TRIP_MESSAGE, QaStartRequest.parse("tripMessage").toRoute())
        assertEquals(AppRoutes.report(), QaStartRequest.parse("report").toRoute())
        assertEquals("report", QaStartRequest.parse("report").toRoute())
        // 30-2 는 피드 전용이라 식별자가 서버 피드 id 다 — 방 표기(`room-22`)는 대상이 아니다
        assertEquals("report?feedId=22", QaStartRequest.parse("report:22").toRoute())
        assertEquals("report", QaStartRequest.parse("report:room-22").toRoute())
        assertEquals(AppRoutes.BLOCKED_USERS, QaStartRequest.parse("blockedUsers").toRoute())
        // 21·20-5·27-3 은 번호별 비교 아트보드라 캡처 라우트가 살아 있어야 한다
        assertEquals(AppRoutes.specialMessages(), QaStartRequest.parse("msgs").toRoute())
        // 21 은 방을 지정해 들어온다 — 캡처 타깃(room-121)이 그 방의 특수 메시지를 그리게 한다
        assertEquals(
            AppRoutes.specialMessages("room-121"),
            QaStartRequest.parse("msgs:room-121").toRoute()
        )
        assertEquals(AppRoutes.COURSE_PUBLISH, QaStartRequest.parse("coursePublish").toRoute())
        assertEquals(AppRoutes.tripDay("room-21"), QaStartRequest.parse("tripDay:room-21").toRoute())
        assertEquals(AppRoutes.NOTIFICATION_DETAIL, QaStartRequest.parse("notifDetail").toRoute())
        assertEquals(AppRoutes.ACCOUNT_DELETE, QaStartRequest.parse("accountDelete").toRoute())
        assertEquals(AppRoutes.SYSTEM_MAINTENANCE, QaStartRequest.parse("maintenance").toRoute())
        assertEquals(AppRoutes.SYSTEM_ERROR, QaStartRequest.parse("error500").toRoute())
        assertEquals(AppRoutes.feedComments("srv-3"), QaStartRequest.parse("feedComments:3").toRoute())
    }

    @Test
    fun everyNumberedDesignManifestIdHasAnAndroidCaptureRoute() {
        val manifestIds = listOf(
            "ds-overview", "splash", "onb-1", "onb-2", "onb-3", "login", "prof-1", "prof-3", "prof-2",
            "email-auth", "terms", "terms-detail", "terms-privacy", "terms-location", "terms-marketing",
            "terms-settings",
            "home", "explore", "explore-map", "search", "notif", "course", "detail", "apply", "create-review",
            "host-manage", "custom-course", "place-search", "place-detail", "create-schedule", "create-meet",
            "create-people", "create-people-small", "create-people-large", "create-detail",
            "create-summary", "create-summary-linked", "course-edit-custom",
            "course-edit-linked", "course-edit-locked", "chat-list", "chat-list-applied", "chat", "chat-menu",
            "chat-attach", "notice-history", "trip-confirmed", "trip-day", "msgs", "feed", "feed-detail",
            "feed-comments", "feed-write", "public-profile", "my", "dex", "trip-message", "friends", "course-publish",
            "profile-edit", "settings", "blocked", "notif-detail", "account-delete", "states", "leave", "report",
            "system-maintenance", "system-error", "offline", "offline-cached", "offline-chat"
        )

        assertEquals(72, manifestIds.size)
        manifestIds.forEach { id ->
            assertTrue("Missing Android QA route for $id", QaStartRequest.parse(id).toRoute() != null)
        }
    }
}
