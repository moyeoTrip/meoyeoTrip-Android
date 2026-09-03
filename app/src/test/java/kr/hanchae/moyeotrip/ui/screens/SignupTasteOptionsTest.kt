package kr.hanchae.moyeotrip.ui.screens

import java.io.File
import kr.hanchae.moyeotrip.data.profile.ProfileOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 06-1 취향 후보는 `GET /api/v1/users/me/profile/options` 가 정본이다(`SIGNUP-GATE-CANON.md` R4).
 *
 * 이 엔드포인트는 **토큰 없이 200** 이라 가입 전에도 부를 수 있다(2026-08-30 실서버 확인).
 * 그래서 클라이언트가 스타일·지역 표를 거울처럼 들고 있으면 안 된다 —
 * 예전 단언(스타일 12개 · 지역 24개)을 **반대로 뒤집어**, 표가 없어야 통과하게 둔다(NO-MOCK-CANON R6).
 */
class SignupTasteOptionsTest {
    @Test
    fun noHardcodedTasteMirrorTableRemains() {
        val sources = File("src/main/java").walkTopDown().filter { it.extension == "kt" }.toList()
        assertTrue("코틀린 소스를 찾지 못했다", sources.isNotEmpty())
        val offenders = sources.filter { file ->
            val text = file.readText()
            text.contains("SignupTravelStyles") || text.contains("SignupInterestedRegions")
        }
        assertEquals("거울 표가 되살아났다: $offenders", emptyList<File>(), offenders)
    }

    @Test
    fun toggleKeepsServerOptionOrderRegardlessOfTapOrder() {
        // 후보 순서는 서버 응답 순서다 — 탭 순서가 아니라 이 순서대로 다시 인코딩한다.
        val options = listOf(ProfileOption(7L, "맛집"), ProfileOption(3L, "바다"), ProfileOption(4L, "산"))
        val afterFirst = encodeToggledTasteId(options, emptySet(), 4L)
        val afterSecond = encodeToggledTasteId(options, decodeTasteIds(afterFirst), 7L)
        assertEquals("7,4", afterSecond)
        assertEquals("4", encodeToggledTasteId(options, decodeTasteIds(afterSecond), 7L))
    }

    @Test
    fun unknownServerIdIsNotEncoded() {
        // 서버 후보에 없는 id 는 인코딩되지 않는다 — 40015·40014 로 거절될 값을 만들지 않는다.
        val options = listOf(ProfileOption(1L, "자연"))
        assertEquals("", encodeToggledTasteId(options, emptySet(), 99L))
    }
}
