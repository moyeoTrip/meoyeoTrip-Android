package kr.hanchae.moyeotrip.data.social

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import org.junit.Assert.assertEquals
import org.junit.Test

class SocialRepositoryTest {
    @Test
    fun friendsParsesFriendshipIdAndUser() = runBlocking {
        val connection = RecordingConnection(
            """{"totalCount":1,"friends":[{"friendshipId":21,"user":{"userId":62,
               "nickname":"따스한 기린 2334","profileImageUrl":null,"introduction":null},
               "lastActive":"방금 전"}]}"""
        )

        val friends = repository(connection).friends()

        assertEquals(21L, friends.single().friendshipId)
        assertEquals(62L, friends.single().user.userId)
        assertEquals("방금 전", friends.single().lastActive)
    }

    /** 친구 삭제 경로는 friendshipId 가 아니라 상대 userId 다 — friendshipId 를 넣으면 서버가 404를 준다. */
    @Test
    fun removeFriendUsesUserIdPath() = runBlocking {
        val connection = RecordingConnection("")
        var requestedPath: String? = null
        val client = MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { url ->
                requestedPath = url.path
                connection
            }
        )

        HttpSocialRepository(client).removeFriend(62)

        assertEquals("/api/v1/users/me/friends/62", requestedPath)
        assertEquals("DELETE", connection.requestMethod)
    }

    @Test
    fun sendRequestAndBlockUseUserIdPaths() = runBlocking {
        val paths = mutableListOf<String>()
        val client = MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { url ->
                paths += url.path
                RecordingConnection("""{"userId":62,"blocked":true}""")
            }
        )
        val repository = HttpSocialRepository(client)

        repository.sendRequest(62)
        repository.block(62)
        repository.unblock(62)

        assertEquals(
            listOf(
                "/api/v1/users/me/friend-requests/62",
                "/api/v1/users/me/blocks/62",
                "/api/v1/users/me/blocks/62"
            ),
            paths
        )
    }

    private fun repository(connection: HttpURLConnection) = HttpSocialRepository(
        MoyeoApiClient(
            baseUrl = "https://example.test",
            accessToken = { "access-token" },
            connectionFactory = { connection }
        )
    )
}

private class RecordingConnection(response: String) : HttpURLConnection(URL("https://example.test")) {
    private val responseBytes = response.toByteArray()

    override fun disconnect() = Unit

    override fun usingProxy(): Boolean = false

    override fun connect() = Unit

    override fun getResponseCode(): Int = if (responseBytes.isEmpty()) HTTP_NO_CONTENT else HTTP_OK

    override fun getInputStream(): InputStream = ByteArrayInputStream(responseBytes)
}
