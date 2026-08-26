package kr.hanchae.moyeotrip.data.api

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * 오류 응답 계약 — 본문은 `{"code": …, "errorMessage": …}` 다.
 * `GET /chat-rooms/{id}/companions` 는 완료 여행 전용이라 상태 코드만으로는 갈라지지 않는다.
 */
class MoyeoApiClientTest {
    @Test
    fun tripNotCompletedIsRecognisedFromStatusAndErrorCode() {
        val error = failingCall(409, """{"code":40915,"errorMessage":"아직 완료되지 않은 여행입니다."}""")

        assertEquals(409, error.statusCode)
        assertEquals(40915, error.errorCode)
        assertEquals("아직 완료되지 않은 여행입니다.", error.message)
        // 권한 오류가 아니다 — 화면은 오류가 아니라 "아직 여행 전"으로 다뤄야 한다.
        assertTrue(error.tripNotCompleted)
    }

    @Test
    fun membershipAndMissingRoomErrorsStayOrdinaryFailures() {
        val notMember = failingCall(403, """{"code":40301,"errorMessage":"참여하지 않은 방입니다."}""")
        val noRoom = failingCall(404, """{"code":40405,"errorMessage":"요청한 리소스를 찾을 수 없습니다."}""")

        assertEquals(40301, notMember.errorCode)
        assertFalse(notMember.tripNotCompleted)
        assertEquals(40405, noRoom.errorCode)
        assertFalse(noRoom.tripNotCompleted)
    }

    @Test
    fun bodiesWithoutCodeKeepWorkingWithoutAnErrorCode() {
        val error = failingCall(500, """{"errorMessage":"서버에러입니다."}""")

        assertNull(error.errorCode)
        assertFalse(error.tripNotCompleted)
        assertEquals("서버에러입니다.", error.message)
    }

    private fun failingCall(statusCode: Int, body: String): MoyeoApiException {
        val client = MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { url -> ErrorConnection(url, statusCode, body) }
        )
        return try {
            runBlocking { client.getObject("/api/v1/chat-rooms/22/companions") }
            fail("오류 응답은 MoyeoApiException 으로 떠야 합니다.")
            error("unreachable")
        } catch (error: MoyeoApiException) {
            error
        }
    }
}

private class ErrorConnection(url: URL, private val statusCode: Int, response: String) : HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = statusCode

    override fun getInputStream(): InputStream = error("에러 응답에서는 errorStream 을 읽어야 합니다.")

    override fun getErrorStream(): InputStream = ByteArrayInputStream(responseBytes)
}
