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
                birthDate = "1998-04-12"
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

    @Test
    fun providerTypeIsNotSentBecauseBackendDerivesItFromFirebaseToken() {
        val fields = signupRequestFields(
            identity = IdentityToken(AuthProvider.KAKAO, "firebase-id-token"),
            input = SignupInput("selection", "따스한 사슴 3492", Gender.FEMALE, "1998-04-12")
        )

        assertFalse(fields.containsKey("providerType"))
    }
}
