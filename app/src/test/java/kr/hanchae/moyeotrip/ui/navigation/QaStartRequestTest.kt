package kr.hanchae.moyeotrip.ui.navigation

import kr.hanchae.moyeotrip.data.network.OfflineExperience
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
        assertEquals(AppRoutes.courseRoute("trip-andong-dosan"), QaStartRequest.parse("courseEditLinked").toRoute())
        assertEquals(AppRoutes.courseRoute("trip-andong-hahoe"), QaStartRequest.parse("courseEditLocked").toRoute())
        assertEquals(
            AppRoutes.noticeHistory("trip-cheongsong-juwangsan"),
            QaStartRequest.parse("noticeHistory").toRoute()
        )
        assertEquals(AppRoutes.MEETINGS_APPLIED, QaStartRequest.parse("chatListApplied").toRoute())
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
}
