package kr.hanchae.moyeotrip.ui.screens

import androidx.compose.ui.graphics.Color
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

    /**
     * 로그인/가입을 따로 고르지 않으므로 "비밀번호 확인" 입력이 없다 —
     * 로그인일 수도 있는 입력에 확인란을 요구할 수 없다.
     */
    @Test
    fun emailAndSixCharacterPasswordAreRequired() {
        assertEquals("이메일 주소를 확인해 주세요.", emailCredentialsError("invalid", "password"))
        assertEquals("비밀번호는 6자 이상 입력해 주세요.", emailCredentialsError("trip@example.com", "12345"))
        assertNull(emailCredentialsError("trip@example.com", "123456"))
    }
}
