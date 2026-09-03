package kr.hanchae.moyeotrip

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import kr.hanchae.moyeotrip.ui.navigation.MoyeoTripApp
import org.junit.Rule
import org.junit.Test

/**
 * 목데이터 제거 회귀 시험 (`docs/alignment/NO-MOCK-CANON.md` R1·R6).
 *
 * 예전 UI 시험은 화면기획 목데이터(주왕산·경주 감성 힐링 코스·따스한 사슴 3492 …)가 **보여야** 통과했다.
 * 이제는 반대다 — 로그인하지 않은 실행에서 그 문자열이 하나라도 보이면 목데이터가 남아 있다는 뜻이다.
 */
class NoMockDataUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun noPlanningMockContentIsRenderedWithoutAServerSession() {
        listOf("home", "explore", "meetings", "feed", "my").forEach { screen ->
            show(screen)
            PLANNING_MOCK_TEXTS.forEach { text ->
                composeRule.onAllNodesWithText(text, substring = true).assertCountEquals(0)
            }
        }
    }

    private fun show(screen: String) {
        composeRule.activity.setContent {
            MoyeoTripApp(startScreen = screen, skipStartupSplash = true, skipAuthentication = true)
        }
        composeRule.waitForIdle()
    }

    private companion object {
        /** 삭제한 목데이터에서만 나오던 문자열들. 하나라도 살아 있으면 목 폴백이 남은 것이다. */
        val PLANNING_MOCK_TEXTS = listOf(
            "주왕산 & 주산지 힐링 트레킹",
            "안동 하회마을 하루 코스",
            "경주 감성 힐링 코스",
            "포항·영덕 동해 드라이브",
            "울릉도 2박 3일 섬 여행",
            "따스한 사슴 3492",
            "숲속여행자",
            "모여트립이"
        )
    }
}
