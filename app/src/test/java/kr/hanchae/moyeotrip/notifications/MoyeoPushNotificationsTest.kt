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
}
