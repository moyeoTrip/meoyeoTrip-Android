package kr.hanchae.moyeotrip.domain.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NicknameSelectionStateTest {
    @Test
    fun nicknameCandidatesProvideVisualMetadataForTheSelectionCards() {
        val candidates = NicknameSelectionState.initial().candidates

        assertTrue(candidates.all { it.description.isNotBlank() })
        assertEquals(listOf("사슴", "거북이", "너구리"), candidates.map { it.animal })
        assertEquals(listOf("RED", "BLUE", "MINT"), candidates.map { it.color })
    }

    @Test
    fun successfulRefreshReplacesTokenAndCandidatesAndClearsSelection() {
        val selected = NicknameSelectionState.initial().select("따스한 사슴 3492")
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
        val selected = NicknameSelectionState.initial().select("따스한 사슴 3492")

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
        val state = NicknameSelectionState.initial()

        assertTrue(state.canRefresh)
        assertEquals("마음에 들 때까지 새 후보를 받아보세요", state.statusMessage)
    }
}
