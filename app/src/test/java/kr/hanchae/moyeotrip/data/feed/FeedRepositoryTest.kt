package kr.hanchae.moyeotrip.data.feed

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 23·23-1 댓글 목록 계약 테스트.
 *
 * 2026-09-02 서버 변경으로 `GET /api/v1/feeds/{feedId}/comments` 응답이
 * **배열에서 `{comments, nextId}` 객체로** 바뀌었다. 배열로 읽던 예전 코드는
 * 예외가 나거나 빈 목록이 됐다 — 그 회귀를 여기서 막는다.
 *
 * 아래 본문·커서 값은 2026-09-02 실서버(feeds/1, 최상위 3 + 답글 2) 응답을 그대로 옮긴 것이다.
 */
class FeedRepositoryTest {
    @Test
    fun commentsParsesObjectResponseAndKeepsRepliesInsideTopLevelComments() = runBlocking {
        val connection = JsonConnection(
            URL("https://example.test/api/v1/feeds/1/comments"),
            """
            {"comments":[
               {"commentId":3,"author":{"userId":61,"nickname":"즐거운 고양이 4760",
                  "profileImageUrl":"https://cdn.test/user/profile/image/a.webp"},
                "content":"이 코스 저도 가보고 싶네요.","createdAt":"2026-08-29T13:08:56.246064",
                "replies":[{"commentId":4,"author":{"userId":61,"nickname":"즐거운 고양이 4760",
                   "profileImageUrl":null},"content":"네 08시 출발이면 여유로워요!",
                   "createdAt":"2026-08-29T13:09:06.979898","replies":[]}]},
               {"commentId":2,"author":{"userId":61,"nickname":"즐거운 고양이 4760",
                  "profileImageUrl":null},"content":"달기약수탕 백숙 진짜 맛있었죠",
                "createdAt":"2026-08-29T13:08:55.890192","replies":[]}],
             "nextId":null}
            """.trimIndent()
        )
        val repository = HttpFeedRepository(client(connection))

        val page = repository.comments(1)

        // 최상위 댓글은 최신 ID 부터 온다 — 서버 순서를 그대로 유지한다.
        assertEquals(listOf(3L, 2L), page.comments.map(FeedComment::commentId))
        assertEquals("즐거운 고양이 4760", page.comments.first().author.nickname)
        assertEquals(
            "https://cdn.test/user/profile/image/a.webp",
            page.comments.first().author.profileImageUrl
        )
        // 대댓글은 최상위 댓글 안에 그대로 들어 있다(페이지네이션 대상이 아니다).
        assertEquals(listOf(4L), page.comments.first().replies.map(FeedComment::commentId))
        assertTrue(page.comments.last().replies.isEmpty())
        // 마지막 묶음이면 nextId 가 null 이라 더 부르지 않는다.
        assertNull(page.nextId)
        assertEquals("Bearer access-token", connection.getRequestProperty("Authorization"))
    }

    @Test
    fun commentsSendsLimitOnFirstRequestAndOmitsCursor() = runBlocking {
        var requested: URL? = null
        val connection = JsonConnection(
            URL("https://example.test/api/v1/feeds/1/comments"),
            """{"comments":[],"nextId":null}"""
        )
        val client = MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { url ->
                requested = url
                connection
            }
        )

        HttpFeedRepository(client).comments(1, limit = 20)

        val query = requested?.query.orEmpty()
        assertTrue("limit 이 실려야 합니다: $query", query.contains("limit=20"))
        // 첫 요청에서는 커서를 생략한다 — 서버가 최신부터 첫 묶음을 준다.
        assertTrue("첫 요청에 beforeCommentId 가 있어서는 안 됩니다: $query", !query.contains("beforeCommentId"))
    }

    @Test
    fun commentsSendsBeforeCommentIdCursorAndReturnsNextId() = runBlocking {
        var requested: URL? = null
        val connection = JsonConnection(
            URL("https://example.test/api/v1/feeds/1/comments"),
            """
            {"comments":[{"commentId":2,"author":{"userId":61,"nickname":"즐거운 고양이 4760",
               "profileImageUrl":null},"content":"달기약수탕 백숙 진짜 맛있었죠",
               "createdAt":"2026-08-29T13:08:55.890192","replies":[]}],
             "nextId":2}
            """.trimIndent()
        )
        val client = MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { url ->
                requested = url
                connection
            }
        )

        val page = HttpFeedRepository(client).comments(1, beforeCommentId = 3, limit = 1)

        val query = requested?.query.orEmpty()
        assertTrue("커서가 실려야 합니다: $query", query.contains("beforeCommentId=3"))
        assertTrue("limit 이 실려야 합니다: $query", query.contains("limit=1"))
        assertEquals(listOf(2L), page.comments.map(FeedComment::commentId))
        // 다음 묶음이 남아 있으면 마지막 최상위 댓글 ID 가 nextId 로 온다(실서버 확인값).
        assertEquals(2L, page.nextId)
    }

    private fun client(connection: HttpURLConnection) = MoyeoApiClient(
        baseUrl = "https://example.test",
        accessToken = { "access-token" },
        connectionFactory = { connection }
    )
}

private class JsonConnection(url: URL, response: String) : HttpURLConnection(url) {
    private val responseBytes = response.toByteArray()
    val requestBody = ByteArrayOutputStream()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = HTTP_OK

    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBytes)

    override fun getOutputStream() = requestBody
}
