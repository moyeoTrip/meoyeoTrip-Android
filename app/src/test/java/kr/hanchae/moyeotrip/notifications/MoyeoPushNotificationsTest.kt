package kr.hanchae.moyeotrip.notifications

import org.junit.Assert.assertEquals
import org.junit.Test

class MoyeoPushNotificationsTest {
    @Test
    fun routesKnownNotificationDestinations() {
        assertEquals("notifications", pushRoute(mapOf("screen" to "notification-center")))
        assertEquals("meetings", pushRoute(mapOf("route" to "chatroom")))
        assertEquals("feed", pushRoute(mapOf("destination" to "post")))
        assertEquals("my", pushRoute(mapOf("screen" to "settings")))
    }

    @Test
    fun unknownDestinationFallsBackHome() {
        assertEquals("home", pushRoute(emptyMap()))
        assertEquals("home", pushRoute(mapOf("screen" to "unexpected")))
    }

    @Test
    fun changedFcmTokenRequiresBackendRegistration() {
        assertEquals(true, tokenRegistrationPending("old-token", "new-token", wasPending = false))
    }

    @Test
    fun unchangedFcmTokenPreservesRegistrationState() {
        assertEquals(false, tokenRegistrationPending("same-token", "same-token", wasPending = false))
        assertEquals(true, tokenRegistrationPending("same-token", "same-token", wasPending = true))
    }

    @Test
    fun consecutiveNotificationsToSameRouteReceiveDistinctEventIds() {
        val first = nextPushNavigationEvent(current = null, route = "meetings")!!
        val second = nextPushNavigationEvent(current = first, route = "meetings")!!

        assertEquals("meetings", first.route)
        assertEquals("meetings", second.route)
        assertEquals(first.id + 1L, second.id)
        assertEquals(null, consumePushNavigationEvent(first, first.id))
        assertEquals(second, consumePushNavigationEvent(second, first.id))
        assertEquals(null, consumePushNavigationEvent(second, second.id))
    }

    @Test
    fun absentPushRouteDoesNotCreateAnEvent() {
        assertEquals(null, nextPushNavigationEvent(current = null, route = null))
    }
}
