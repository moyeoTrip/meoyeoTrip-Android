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

    @Test
    fun keepsDefaultRoutesForScreensWithoutExplicitIds() {
        assertEquals(AppRoutes.feedDetail("feed-1"), QaStartRequest.parse("feed-detail").toRoute())
        assertEquals(
            AppRoutes.courseDetail("cheongsong-juwangsan"),
            QaStartRequest.parse("course").toRoute()
        )
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
        assertEquals(AppRoutes.createRecruitment("cheongsong-juwangsan"), QaStartRequest.parse("create").toRoute())
        assertEquals(
            AppRoutes.customCourse("draft-cheongsong-juwangsan"),
            QaStartRequest.parse("customCourse").toRoute()
        )
        assertEquals(
            AppRoutes.createSchedule("draft-cheongsong-juwangsan"),
            QaStartRequest.parse("createSchedule").toRoute()
        )
        assertEquals(
            AppRoutes.createMeetPoint("draft-cheongsong-juwangsan"),
            QaStartRequest.parse("createMeet").toRoute()
        )
        assertEquals(
            AppRoutes.createSummary("draft-cheongsong-juwangsan"),
            QaStartRequest.parse("createSummary").toRoute()
        )
        assertEquals(AppRoutes.courseRoute("trip-cheongsong-juwangsan"), QaStartRequest.parse("courseEdit").toRoute())
        assertEquals(
            AppRoutes.courseRoute("trip-cheongsong-juwangsan-linked"),
            QaStartRequest.parse("courseEditLinked").toRoute()
        )
        assertEquals(
            AppRoutes.courseRoute("trip-cheongsong-juwangsan-locked"),
            QaStartRequest.parse("courseEditLocked").toRoute()
        )
        assertEquals(
            AppRoutes.noticeHistory("trip-cheongsong-juwangsan"),
            QaStartRequest.parse("noticeHistory").toRoute()
        )
        assertEquals(AppRoutes.MEETINGS_APPLIED, QaStartRequest.parse("chatListApplied").toRoute())
    }

    @Test
    fun opensChangeLogSixAndSevenCaptureStatesDirectly() {
        val draftId = "draft-cheongsong-juwangsan"
        assertEquals(AppRoutes.placeSearch(draftId), QaStartRequest.parse("place-search").toRoute())
        assertEquals(
            AppRoutes.placeDetail(draftId, "2299341"),
            QaStartRequest.parse("place-detail").toRoute()
        )
        assertEquals(AppRoutes.createDetail(draftId), QaStartRequest.parse("create-detail").toRoute())
        assertEquals(AppRoutes.createPeople(draftId), QaStartRequest.parse("create-people").toRoute())
        assertEquals(AppRoutes.createSummary(draftId), QaStartRequest.parse("create-summary-linked").toRoute())
        assertEquals(
            kr.hanchae.moyeotrip.data.CourseSource.Linked,
            kr.hanchae.moyeotrip.data.MockTripRepository.findRecruitmentDraft(draftId).courseSource
        )
        assertEquals(AppRoutes.createSummary(draftId), QaStartRequest.parse("create-summary-custom").toRoute())
        assertEquals(
            kr.hanchae.moyeotrip.data.CourseSource.Custom,
            kr.hanchae.moyeotrip.data.MockTripRepository.findRecruitmentDraft(draftId).courseSource
        )
        assertEquals(AppRoutes.termsDetail("service", "signup"), QaStartRequest.parse("terms-detail").toRoute())
        assertEquals(AppRoutes.termsDetail("privacy", "signup"), QaStartRequest.parse("terms-privacy").toRoute())
        assertEquals(AppRoutes.termsDetail("location", "signup"), QaStartRequest.parse("terms-location").toRoute())
        assertEquals(AppRoutes.termsDetail("marketing", "signup"), QaStartRequest.parse("terms-marketing").toRoute())
        assertEquals(AppRoutes.termsDetail("service", "settings"), QaStartRequest.parse("terms-settings").toRoute())
        assertEquals(AppRoutes.mockAuth("email"), QaStartRequest.parse("email-auth").toRoute())
        assertEquals(
            AppRoutes.qaApply("trip-cheongsong-juwangsan"),
            QaStartRequest.parse("apply").toRoute()
        )
        assertEquals(AppRoutes.QA_LEAVE, QaStartRequest.parse("leave").toRoute())
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
        assertEquals(
            AppRoutes.chatRoom("chat-cheongsong-juwangsan"),
            QaStartRequest.parse("offlineChat").toRoute()
        )
        assertEquals(
            OfflineExperience.Cached,
            QaStartRequest.parse("offlineChat").offlineExperienceOverride
        )
        assertEquals(AppRoutes.TRIP_CONFIRMED, QaStartRequest.parse("tripConfirmed").toRoute())
    }

    @Test
    fun opensChangeLogManagementSafetyAndSystemScreensDirectly() {
        val chatId = "chat-cheongsong-juwangsan"
        assertEquals(AppRoutes.chatMenu(chatId), QaStartRequest.parse("chatMenu:$chatId").toRoute())
        assertEquals(AppRoutes.CHAT_ATTACH, QaStartRequest.parse("chatAttach").toRoute())
        assertEquals(AppRoutes.FRIENDS, QaStartRequest.parse("friends").toRoute())
        assertEquals(AppRoutes.TRIP_MESSAGE, QaStartRequest.parse("tripMessage").toRoute())
        assertEquals(AppRoutes.REPORT, QaStartRequest.parse("report").toRoute())
        assertEquals(AppRoutes.BLOCKED_USERS, QaStartRequest.parse("blockedUsers").toRoute())
        assertEquals(AppRoutes.COURSE_PUBLISH, QaStartRequest.parse("coursePublish").toRoute())
        assertEquals(AppRoutes.tripDay(chatId), QaStartRequest.parse("tripDay:$chatId").toRoute())
        assertEquals(AppRoutes.NOTIFICATION_DETAIL, QaStartRequest.parse("notifDetail").toRoute())
        assertEquals(AppRoutes.ACCOUNT_DELETE, QaStartRequest.parse("accountDelete").toRoute())
        assertEquals(AppRoutes.SYSTEM_MAINTENANCE, QaStartRequest.parse("maintenance").toRoute())
        assertEquals(AppRoutes.SYSTEM_ERROR, QaStartRequest.parse("error500").toRoute())
        assertEquals(AppRoutes.feedComments("feed-3"), QaStartRequest.parse("feedComments:feed-3").toRoute())
    }

    @Test
    fun everyNumberedDesignManifestIdHasAnAndroidCaptureRoute() {
        val manifestIds = listOf(
            "ds-overview", "splash", "onb-1", "onb-2", "onb-3", "login", "prof-1", "prof-3", "prof-2",
            "email-auth", "terms", "terms-detail", "terms-privacy", "terms-location", "terms-marketing",
            "terms-settings",
            "home", "explore", "explore-map", "search", "notif", "course", "detail", "apply", "create-review",
            "host-manage", "custom-course", "place-search", "place-detail", "create-schedule", "create-meet",
            "create-people", "create-detail", "create-summary", "create-summary-linked", "course-edit-custom",
            "course-edit-linked", "course-edit-locked", "chat-list", "chat-list-applied", "chat", "chat-menu",
            "chat-attach", "notice-history", "trip-confirmed", "trip-day", "msgs", "feed", "feed-detail",
            "feed-comments", "feed-write", "public-profile", "my", "dex", "trip-message", "friends", "course-publish",
            "profile-edit", "settings", "blocked", "notif-detail", "account-delete", "states", "leave", "report",
            "system-maintenance", "system-error", "offline", "offline-cached", "offline-chat"
        )

        assertEquals(70, manifestIds.size)
        manifestIds.forEach { id ->
            assertTrue("Missing Android QA route for $id", QaStartRequest.parse(id).toRoute() != null)
        }
    }
}
