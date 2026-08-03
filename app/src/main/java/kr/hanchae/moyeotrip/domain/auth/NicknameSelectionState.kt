package kr.hanchae.moyeotrip.domain.auth

const val NICKNAME_REFRESH_ERROR = "새 이름을 불러오지 못했어요. 다시 시도해주세요."

data class NicknameSelectionState(
    val selectionToken: String,
    val candidates: List<NicknameCandidate>,
    val selectedNickname: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canRefresh: Boolean
        get() = !isLoading

    val canContinue: Boolean
        get() = !isLoading && selectedNickname != null

    val statusMessage: String
        get() = when {
            errorMessage != null -> errorMessage
            else -> "마음에 들 때까지 새 후보를 받아보세요"
        }

    fun select(nickname: String): NicknameSelectionState {
        if (isLoading || candidates.none { it.nickname == nickname }) return this
        return copy(selectedNickname = nickname)
    }

    fun beginRefresh(): NicknameSelectionState {
        if (!canRefresh) return this
        return copy(isLoading = true, errorMessage = null)
    }

    fun completeRefresh(response: NicknameCandidateResponse): NicknameSelectionState {
        require(response.candidates.size == 3) { "Nickname refresh must return exactly three candidates." }
        return copy(
            selectionToken = response.selectionToken,
            candidates = response.candidates,
            selectedNickname = null,
            isLoading = false,
            errorMessage = null
        )
    }

    fun failRefresh(message: String = NICKNAME_REFRESH_ERROR): NicknameSelectionState = copy(
        isLoading = false,
        errorMessage = message
    )

    companion object {
        fun empty(): NicknameSelectionState = NicknameSelectionState(
            selectionToken = "",
            candidates = emptyList(),
            isLoading = true
        )

        fun fromInitial(response: NicknameCandidateResponse): NicknameSelectionState {
            require(response.candidates.size == 3) {
                "Initial nickname response must contain exactly three candidates."
            }
            return NicknameSelectionState(
                selectionToken = response.selectionToken,
                candidates = response.candidates
            )
        }

        fun initial(): NicknameSelectionState = NicknameSelectionState(
            selectionToken = MockNicknameCandidateGateway.initialResponse.selectionToken,
            candidates = MockNicknameCandidateGateway.initialResponse.candidates
        )
    }
}
