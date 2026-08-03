package kr.hanchae.moyeotrip.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QaStartRequestTest {
    @Test
    fun parsesDirectLaunchScreensWithExplicitIds() {
        assertEquals(
            AppRoutes.feedDetail("feed-3"),
            QaStartRequest.parse("feed-detail:feed-3")?.toRoute()
        )
        assertEquals(
            AppRoutes.courseDetail("course-andong-hahoe"),
            QaStartRequest.parse("course-detail:course-andong-hahoe")?.toRoute()
        )
        assertEquals(
            AppRoutes.tripDetail("trip-ulleung-island"),
            QaStartRequest.parse("trip-detail:trip-ulleung-island")?.toRoute()
        )
        assertEquals(
            AppRoutes.chatRoom("chat-cheongsong-juwangsan"),
            QaStartRequest.parse("chat:chat-cheongsong-juwangsan")?.toRoute()
        )
    }

    @Test
    fun keepsDefaultRoutesForScreensWithoutExplicitIds() {
        assertEquals(AppRoutes.feedDetail("feed-1"), QaStartRequest.parse("feed-detail")?.toRoute())
        assertEquals(
            AppRoutes.courseDetail("cheongsong-juwangsan"),
            QaStartRequest.parse("course")?.toRoute()
        )
        assertEquals(AppRoutes.HOME, QaStartRequest.parse(null)?.toRoute())
    }

    @Test
    fun opensMyHubSupportScreensBySharedQaNames() {
        assertEquals(AppRoutes.MY_FEED, QaStartRequest.parse("my-feed")?.toRoute())
        assertEquals(AppRoutes.MY_FEED, QaStartRequest.parse("myFeed")?.toRoute())
        assertEquals(AppRoutes.CUSTOMER_CENTER, QaStartRequest.parse("customer-center")?.toRoute())
        assertEquals(AppRoutes.CUSTOMER_CENTER, QaStartRequest.parse("customerCenter")?.toRoute())
    }

    @Test
    fun treatsExploreMapSpellingVariantsAsMapMode() {
        assertTrue(QaStartRequest.parse("explore-map")?.startsInExploreMap == true)
        assertTrue(QaStartRequest.parse("exploreMap")?.startsInExploreMap == true)
        assertEquals(AppRoutes.EXPLORE, QaStartRequest.parse("map")?.toRoute())
    }
}
