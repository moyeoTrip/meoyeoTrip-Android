package kr.hanchae.moyeotrip.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationsTest {
    @Test
    fun bottomTabsExposeStableUniqueRoutes() {
        val routes = BottomTab.entries.map { it.route }

        assertEquals(BottomTab.entries.size, routes.toSet().size)
        assertEquals(AppRoutes.HOME, BottomTab.Home.route)
        assertEquals(AppRoutes.MEETINGS, BottomTab.Meetings.route)
        assertEquals(listOf("홈", "탐색", "모임", "피드", "마이"), BottomTab.entries.map { it.label })
    }

    @Test
    fun dynamicRoutesBuildExpectedPaths() {
        assertEquals("course/andong-hahoe", AppRoutes.courseDetail("andong-hahoe"))
        assertEquals("trip/trip-andong-hahoe", AppRoutes.tripDetail("trip-andong-hahoe"))
        assertEquals("feed/feed-1", AppRoutes.feedDetail("feed-1"))
        assertEquals("chat/chat-andong", AppRoutes.chatRoom("chat-andong"))
        assertEquals("create_recruitment/andong-hahoe", AppRoutes.createRecruitment("andong-hahoe"))
        assertTrue(AppRoutes.COURSE_DETAIL.contains("{courseId}"))
        assertTrue(AppRoutes.TRIP_DETAIL.contains("{tripId}"))
        assertTrue(AppRoutes.FEED_DETAIL.contains("{postId}"))
        assertTrue(AppRoutes.CHAT_ROOM.contains("{threadId}"))
        assertTrue(AppRoutes.CREATE_RECRUITMENT.contains("{courseId}"))
    }
}
