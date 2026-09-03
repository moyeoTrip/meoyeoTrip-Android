package kr.hanchae.moyeotrip.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 목데이터 제거 회귀 시험 (`docs/alignment/NO-MOCK-CANON.md` R1).
 *
 * 예전 `MockTripRepositoryTest` 는 예시 코스·모집·채팅방이 **있어야** 통과했다.
 * 이제는 그 반대다 — 초안은 비어 있는 상태로 시작해야 하고, 앱이 들고 있는 예시 데이터는 없어야 한다.
 */
class RecruitmentDraftStoreTest {
    @After
    fun tearDown() {
        RecruitmentDraftStore.clearForTests()
    }

    @Test
    fun newDraftStartsEmpty() {
        val draft = RecruitmentDraftStore.draft("new")

        assertEquals("draft-new", draft.id)
        assertTrue(draft.recruitmentName.isEmpty())
        assertTrue(draft.travelDate.isEmpty())
        assertTrue(draft.recruitmentDeadline.isEmpty())
        assertTrue(draft.note.isEmpty())
        assertTrue(draft.routeStops.isEmpty())
        assertTrue(draft.meetingLocation.name.isEmpty())
        assertEquals(0, draft.estimatedCostPerPerson)
        assertNull(draft.serverCourseId)
        assertNull(draft.serverCourseTitle)
    }

    @Test
    fun sameKeyReturnsTheSameDraft() {
        val first = RecruitmentDraftStore.draft("new")
        RecruitmentDraftStore.update(first.copy(recruitmentName = "천천히 걷는 모임"))

        assertEquals("천천히 걷는 모임", RecruitmentDraftStore.draft("new").recruitmentName)
        assertEquals("천천히 걷는 모임", RecruitmentDraftStore.draft("draft-new").recruitmentName)
    }

    /** 빈 초안은 오류가 아니라 "아직 덜 채운 상태"다 — 예외 대신 막는 이유를 돌려준다. */
    @Test
    fun emptyDraftCannotOpenARecruitment() {
        assertNotNull(RecruitmentDraftStore.validationError(RecruitmentDraftStore.draft("new")))
    }

    @Test
    fun filledDraftWithServerCourseCanOpenARecruitment() {
        val draft = RecruitmentDraftStore.draft("new").copy(
            recruitmentName = "천천히 걷는 모임",
            travelDate = "2026.05.25 (토)",
            recruitmentDeadline = "2026.05.22 (금) 23:59",
            meetingLocation = MeetingLocation("청송 시외버스터미널", "정문 앞", 36.4356, 129.0572, "07:50"),
            routeStops = listOf(
                RouteStop("stop-1", time = "09:00", name = "청송 시외버스터미널", memo = ""),
                RouteStop("stop-2", time = "10:30", name = "주왕산 국립공원", memo = "")
            ),
            serverCourseId = 21
        )

        assertNull(RecruitmentDraftStore.validationError(draft))
    }

    @Test
    fun removedDraftComesBackEmpty() {
        RecruitmentDraftStore.update(RecruitmentDraftStore.draft("new").copy(recruitmentName = "지워질 초안"))
        RecruitmentDraftStore.remove("draft-new")

        assertTrue(RecruitmentDraftStore.draft("new").recruitmentName.isEmpty())
    }
}
