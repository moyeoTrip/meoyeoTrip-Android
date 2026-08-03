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
}
