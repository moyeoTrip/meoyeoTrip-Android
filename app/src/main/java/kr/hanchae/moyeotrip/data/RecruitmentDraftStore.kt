package kr.hanchae.moyeotrip.data

import androidx.compose.runtime.mutableStateMapOf

/**
 * 모집 만들기(17-1 ~ 17-7) 초안 보관소.
 *
 * 서버에는 "작성 중인 모집"을 담아둘 API 가 없다 — 마지막 단계에서 POST chat-rooms 한 번으로 만든다.
 * 그래서 단계 사이를 잇는 값만 앱 세션 안에 들고 있는다. **여기에 예시 코스·예시 일정을 심지 않는다.**
 * 초안은 항상 빈 값으로 시작하고, 채워지는 값은 전부 사용자 입력이거나 서버 응답이다.
 */
object RecruitmentDraftStore {
    private val drafts = mutableStateMapOf<String, RecruitmentDraft>()

    /** 이미 있으면 그대로, 없으면 빈 초안을 만든다. [key] 는 라우트가 들고 다니는 초안 식별자다. */
    fun draft(key: String): RecruitmentDraft {
        val draftId = draftId(key)
        return drafts.getOrPut(draftId) { RecruitmentDraft(id = draftId) }
    }

    fun update(draft: RecruitmentDraft) {
        drafts[draft.id] = draft
    }

    fun remove(draftId: String) {
        drafts.remove(draftId)
    }

    /**
     * 모집을 열 수 있는 상태인지. 화면은 이 결과로 "모집 열기" 버튼을 막고 이유를 보여준다.
     * 예외로 던지지 않는 이유는, 초안이 비어 있는 것은 오류가 아니라 아직 덜 채운 상태이기 때문이다.
     */
    fun validationError(draft: RecruitmentDraft): String? = when {
        draft.recruitmentName.isBlank() -> "모집 이름을 입력해 주세요."

        draft.travelDate.isBlank() -> "여행 날짜를 정해 주세요."

        draft.recruitmentDeadline.isBlank() -> "모집 마감일을 정해 주세요."

        draft.meetingLocation.name.isBlank() -> "집합 장소를 정해 주세요."

        draft.routeStops.size < MIN_ROUTE_STOPS -> "방문지는 2개 이상 20개 이하여야 해요."

        draft.routeStops.size > MAX_ROUTE_STOPS -> "방문지는 2개 이상 20개 이하여야 해요."

        draft.minimumAge !in AGE_RANGE || draft.maximumAge !in AGE_RANGE ->
            "연령 제한은 20세 이상 100세 이하로 설정해 주세요."

        draft.minimumAge > draft.maximumAge -> "최소 나이는 최대 나이보다 클 수 없어요."

        draft.estimatedCostPerPerson < 0 -> "예상 비용은 0원 이상이어야 해요."

        draft.serverCourseId == null -> "코스를 먼저 골라 주세요."

        else -> null
    }

    /** 테스트가 세션 상태를 남기지 않도록 비운다. */
    fun clearForTests() {
        drafts.clear()
    }

    const val MIN_ROUTE_STOPS = 2
    const val MAX_ROUTE_STOPS = 20

    /** 17-1 커스텀 코스가 나눌 수 있는 날 수 상한. 방문지 상한(20)보다 커질 이유가 없다. */
    const val MAX_COURSE_DAYS = 10
    private val AGE_RANGE = 20..100

    private fun draftId(key: String): String {
        val normalized = key.trim()
        return if (normalized.isEmpty()) "draft-new" else normalized.removePrefix("draft-").let { "draft-$it" }
    }
}
