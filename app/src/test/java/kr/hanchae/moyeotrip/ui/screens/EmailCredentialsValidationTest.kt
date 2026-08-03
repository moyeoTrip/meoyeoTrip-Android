package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.ui.graphics.Color
import kr.hanchae.moyeotrip.domain.auth.EmailAuthAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmailCredentialsValidationTest {
    @Test
    fun googleBrandPaletteMatchesOfficialLightAndDarkValues() {
        val light = googleButtonPalette(false)
        val dark = googleButtonPalette(true)

        assertEquals(Color.White, light.container)
        assertEquals(Color(0xFF747775), light.border)
        assertEquals(Color(0xFF1F1F1F), light.label)
        assertEquals(Color(0xFF131314), dark.container)
        assertEquals(Color(0xFF8E918F), dark.border)
        assertEquals(Color(0xFFE3E3E3), dark.label)
    }

    @Test
    fun signInRequiresValidEmailAndSixCharacterPassword() {
        assertEquals(
            "이메일 주소를 확인해 주세요.",
            emailCredentialsError("invalid", "password", "", EmailAuthAction.SIGN_IN)
        )
        assertEquals(
            "비밀번호는 6자 이상 입력해 주세요.",
            emailCredentialsError("trip@example.com", "12345", "", EmailAuthAction.SIGN_IN)
        )
        assertNull(emailCredentialsError("trip@example.com", "123456", "", EmailAuthAction.SIGN_IN))
    }

    @Test
    fun accountCreationRequiresMatchingPasswordConfirmation() {
        assertEquals(
            "비밀번호가 일치하지 않아요.",
            emailCredentialsError("trip@example.com", "password123", "different", EmailAuthAction.CREATE_ACCOUNT)
        )
        assertNull(
            emailCredentialsError(
                "trip@example.com",
                "password123",
                "password123",
                EmailAuthAction.CREATE_ACCOUNT
            )
        )
    }
}
