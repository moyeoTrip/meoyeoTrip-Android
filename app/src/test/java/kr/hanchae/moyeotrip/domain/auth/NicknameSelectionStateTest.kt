package kr.hanchae.moyeotrip.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NicknameSelectionStateTest {
    /** 서버 응답을 흉내 낸 **시험 전용** 값이다 — 앱에는 후보를 만드는 코드가 없다. */
    private fun serverResponse(): NicknameCandidateResponse = NicknameCandidateResponse(
        selectionToken = "server-token-1",
        candidates = listOf("느긋한 사슴 1001", "잔잔한 거북이 1002", "호기심 많은 너구리 1003")
            .mapIndexed { index, nickname ->
                NicknameCandidate(nickname, color = listOf("RED", "BLUE", "MINT")[index])
            }
    )

    private fun initial(): NicknameSelectionState = NicknameSelectionState.fromInitial(serverResponse())

    /** 후보의 동물·색은 서버 닉네임에서 뽑는다 — 앱이 목록을 들고 있지 않다. */
    @Test
    fun nicknameCandidatesProvideVisualMetadataForTheSelectionCards() {
        val candidates = initial().candidates

        assertTrue(candidates.all { it.description.isNotBlank() })
        assertEquals(listOf("사슴", "거북이", "너구리"), candidates.map { it.animal })
        assertEquals(listOf("RED", "BLUE", "MINT"), candidates.map { it.color })
    }

    @Test
    fun successfulRefreshReplacesTokenAndCandidatesAndClearsSelection() {
        val selected = initial().select("느긋한 사슴 1001")
        val response = NicknameCandidateResponse(
            selectionToken = "server-token-2",
            candidates = listOf("새 이름 1001", "새 이름 1002", "새 이름 1003").map { NicknameCandidate(it) }
        )

        val refreshed = selected.beginRefresh().completeRefresh(response)

        assertEquals("server-token-2", refreshed.selectionToken)
        assertEquals(response.candidates, refreshed.candidates)
        assertNull(refreshed.selectedNickname)
        assertFalse(refreshed.isLoading)
        assertFalse(refreshed.canContinue)
    }

    @Test
    fun failedRefreshKeepsExistingCandidatesAndSelection() {
        val selected = initial().select("느긋한 사슴 1001")

        val failed = selected.beginRefresh().failRefresh()

        assertEquals(selected.selectionToken, failed.selectionToken)
        assertEquals(selected.candidates, failed.candidates)
        assertEquals(selected.selectedNickname, failed.selectedNickname)
        assertEquals(NICKNAME_REFRESH_ERROR, failed.errorMessage)
        assertEquals(NICKNAME_REFRESH_ERROR, failed.statusMessage)
        assertTrue(failed.canContinue)
    }

    @Test
    fun successfulRefreshCanBeRepeatedBecauseServerDefinesNoNicknameLimit() {
        val state = initial()

        assertTrue(state.canRefresh)
        assertEquals("마음에 들 때까지 새 후보를 받아보세요", state.statusMessage)
    }
}
