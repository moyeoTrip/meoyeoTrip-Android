package kr.hanchae.moyeotrip.data.auth

import kr.hanchae.moyeotrip.domain.auth.AuthProvider
import kr.hanchae.moyeotrip.domain.auth.Gender
import kr.hanchae.moyeotrip.domain.auth.IdentityToken
import kr.hanchae.moyeotrip.domain.auth.SignupInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SignupRequestFieldsTest {
    @Test
    fun correctedSignupPayloadContainsOnlyBackendContractFields() {
        val fields = signupRequestFields(
            identity = IdentityToken(AuthProvider.APPLE, "id-token", "fcm-token"),
            input = SignupInput(
                nicknameSelectionToken = "selection-token",
                nickname = "따스한 사슴 3492",
                gender = Gender.FEMALE,
                birthDate = "1998-04-12",
                agreedTermIds = listOf(1L, 2L)
            )
        )

        assertEquals(
            setOf("idToken", "nicknameSelectionToken", "nickname", "gender", "birthDate", "fcmToken"),
            fields.keys
        )
        assertEquals("F", fields["gender"])
        assertEquals("1998-04-12", fields["birthDate"])
        assertFalse(fields.containsKey("terms"))
    }

    /**
     * 동의한 약관 ID 는 서버 필수 항목이다 — 빠지면 400 40012 로 가입이 막힌다.
     * 문자열 맵으로는 배열을 담을 수 없어 [signupRequestBody] 가 따로 얹는다.
     */
    @Test
    fun signupBodyCarriesAgreedTermIdsAsArray() {
        val body = signupRequestBody(
            identity = IdentityToken(AuthProvider.APPLE, "id-token"),
            input = SignupInput("selection", "따스한 사슴 3492", Gender.FEMALE, "1998-04-12", listOf(2L, 1L))
        )

        val ids = body.getJSONArray("agreedTermIds")
        assertEquals(2, ids.length())
        assertEquals(2L, ids.getLong(0))
        assertEquals(1L, ids.getLong(1))
    }

    /**
     * 07 취향 단계에서 고른 값은 이 요청이 유일한 전송 지점이다.
     * 빠지면 사용자가 고른 취향이 조용히 유실된다(정본 R4).
     */
    @Test
    fun signupBodyCarriesSelectedTasteIdsAsServerIds() {
        val body = signupRequestBody(
            identity = IdentityToken(AuthProvider.KAKAO, "id-token"),
            input = SignupInput(
                nicknameSelectionToken = "selection",
                nickname = "따스한 사슴 3492",
                gender = Gender.FEMALE,
                birthDate = "1998-04-12",
                agreedTermIds = listOf(1L),
                travelStyleIds = listOf(1L, 3L),
                interestedRegionIds = listOf(1L, 17L)
            )
        )

        val styles = body.getJSONArray("travelStyleIds")
        val regions = body.getJSONArray("interestedRegionIds")
        assertEquals(listOf(1L, 3L), List(styles.length()) { styles.getLong(it) })
        assertEquals(listOf(1L, 17L), List(regions.length()) { regions.getLong(it) })
    }

    /** 선택 항목이다 — 고르지 않았으면 빈 배열이 아니라 필드 자체를 뺀다. */
    @Test
    fun signupBodyOmitsTasteFieldsWhenNothingWasSelected() {
        val body = signupRequestBody(
            identity = IdentityToken(AuthProvider.KAKAO, "id-token"),
            input = SignupInput("selection", "따스한 사슴 3492", Gender.FEMALE, "1998-04-12", listOf(1L))
        )

        assertFalse(body.has("travelStyleIds"))
        assertFalse(body.has("interestedRegionIds"))
    }

    /** 공백만 있는 FCM 토큰은 서버가 `40016` 으로 막는다 — 아예 싣지 않는다(정본 R6). */
    @Test
    fun blankFcmTokenIsOmittedInsteadOfBeingSentAsWhitespace() {
        val fields = signupRequestFields(
            identity = IdentityToken(AuthProvider.GOOGLE, "id-token", "   "),
            input = SignupInput("selection", "따스한 사슴 3492", Gender.FEMALE, "1998-04-12", listOf(1L))
        )

        assertFalse(fields.containsKey("fcmToken"))
    }

    @Test
    fun providerTypeIsNotSentBecauseBackendDerivesItFromFirebaseToken() {
        val fields = signupRequestFields(
            identity = IdentityToken(AuthProvider.KAKAO, "firebase-id-token"),
            input = SignupInput("selection", "따스한 사슴 3492", Gender.FEMALE, "1998-04-12", listOf(1L))
        )

        assertFalse(fields.containsKey("providerType"))
    }
}
