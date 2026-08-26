package kr.hanchae.moyeotrip.data.notifications

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 화면기획 20-1 "이 모임의 알림만 끄기" — GET·PUT notifications/settings/chat-rooms/{roomId}. */
class RoomNotificationSettingTest {
    @Test
    fun roomSettingReadsEnabledFlag() = runBlocking {
        val call = Call("""{"roomId":21,"enabled":false}""")
        val setting = HttpNotificationRepository(call.client()).roomSetting(21)

        assertEquals("GET", call.method)
        assertEquals("/api/v1/notifications/settings/chat-rooms/21", call.path)
        assertEquals(21L, setting.roomId)
        assertFalse(setting.enabled)
    }

    @Test
    fun updateRoomSettingSendsEnabledAndReturnsSavedValue() = runBlocking {
        val call = Call("""{"roomId":21,"enabled":true}""")
        val saved = HttpNotificationRepository(call.client()).updateRoomSetting(21, true)

        assertEquals("PUT", call.method)
        assertEquals("/api/v1/notifications/settings/chat-rooms/21", call.path)
        assertTrue(JSONObject(call.body()).getBoolean("enabled"))
        assertTrue(saved.enabled)
    }

    /** 서버 기본은 수신이다 — 응답에 필드가 없으면 "꺼짐"으로 읽어서는 안 된다. */
    @Test
    fun missingEnabledFieldFallsBackToOn() = runBlocking {
        val call = Call("""{"roomId":21}""")
        val setting = HttpNotificationRepository(call.client()).roomSetting(21)

        assertTrue(setting.enabled)
    }

    private class Call(private val response: String) {
        lateinit var method: String
            private set
        lateinit var path: String
            private set
        private val recorded = ByteArrayOutputStream()

        fun body(): String = recorded.toString(Charsets.UTF_8.name())

        fun client() = MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { url -> Connection(url) }
        )

        private inner class Connection(url: URL) : HttpURLConnection(url) {
            init {
                path = url.path
            }

            override fun setRequestMethod(method: String) {
                super.setRequestMethod(method)
                this@Call.method = method
            }

            override fun disconnect() = Unit

            override fun usingProxy(): Boolean = false

            override fun connect() = Unit

            override fun getResponseCode(): Int = HTTP_OK

            override fun getInputStream(): InputStream = ByteArrayInputStream(response.toByteArray())

            override fun getOutputStream() = recorded
        }
    }
}
