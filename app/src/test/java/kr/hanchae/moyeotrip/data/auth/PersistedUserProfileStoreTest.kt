package kr.hanchae.moyeotrip.data.auth

import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersistedUserProfileStoreTest {
    @Test
    fun readsCapitalNNickNameClaimFromAccessTokenPayload() {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"userId":12,"nickName":"따스한 사슴 3492"}""".toByteArray())

        assertEquals("따스한 사슴 3492", "header.$payload.signature".nicknameClaim())
    }

    @Test
    fun doesNotAcceptDifferentNicknameClaimCasing() {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"nickname":"잘못된 키"}""".toByteArray())

        assertNull("header.$payload.signature".nicknameClaim())
    }

    /**
     * 채팅 정렬이 이 값에 걸려 있다. 멤버 목록의 `me` 를 기다리면 내 메시지가 한 프레임 동안
     * 왼쪽에 그려진다 — 토큰에서 바로 읽어야 첫 프레임부터 맞는다(정본 R6).
     */
    @Test
    fun readsUserIdClaimFromAccessTokenPayload() {
        val payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("""{"userId":"62","nickName":"여행자"}""".toByteArray())

        assertEquals(62L, "header.$payload.signature".userIdClaim())
    }

    @Test
    fun userIdClaimIsMissingOnMalformedToken() {
        assertNull("not-a-jwt".userIdClaim())
    }
}
