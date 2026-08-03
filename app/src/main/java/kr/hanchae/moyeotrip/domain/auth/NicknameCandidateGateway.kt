package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.delay

const val NICKNAME_CANDIDATE_ENDPOINT = "/api/v1/auth/nickname-candidates"

data class NicknameCandidate(
    val nickname: String,
    val adjective: String = nickname.substringBefore(' '),
    val animal: String = nickname.split(' ').dropLast(1).lastOrNull().orEmpty(),
    val color: String = "MINT",
    val description: String = "함께 천천히 경북을 둘러보는 여행자예요"
)

data class NicknameCandidateResponse(val selectionToken: String, val candidates: List<NicknameCandidate>)

fun interface NicknameCandidateGateway {
    suspend fun fetchCandidates(): NicknameCandidateResponse
}

/**
 * Preview implementation of the backend nickname-candidate contract.
 * Replace this gateway with the API implementation once the app has a shared network layer.
 */
class MockNicknameCandidateGateway : NicknameCandidateGateway {
    private var requestIndex = -1

    override suspend fun fetchCandidates(): NicknameCandidateResponse {
        delay(560)
        requestIndex = (requestIndex + 1).coerceAtMost(candidateBatches.lastIndex)
        if (requestIndex == 0) return initialResponse
        return NicknameCandidateResponse(
            selectionToken = "mock-selection-$requestIndex",
            candidates = candidateBatches[requestIndex].mapIndexed { index, nickname ->
                NicknameCandidate(
                    nickname = nickname,
                    color = mockColors[(requestIndex * 3 + index) % mockColors.size]
                )
            }
        )
    }

    companion object {
        val initialResponse = NicknameCandidateResponse(
            selectionToken = "mock-selection-0",
            candidates = listOf(
                "따스한 사슴 3492",
                "잔잔한 거북이 1108",
                "호기심 많은 너구리 9027"
            ).mapIndexed { index, nickname ->
                NicknameCandidate(nickname, color = listOf("RED", "BLUE", "MINT")[index])
            }
        )

        private val candidateBatches = listOf(
            initialResponse.candidates.map(NicknameCandidate::nickname),
            listOf("포근한 수달 4712", "용감한 다람쥐 6835", "느긋한 부엉이 2054"),
            listOf("반가운 여우 7316", "든든한 곰 8840", "산뜻한 토끼 1269"),
            listOf("다정한 두루미 5931", "명랑한 고양이 3407", "차분한 고라니 9182"),
            listOf("싱그러운 담비 6503", "씩씩한 강아지 2784", "온화한 백로 4176")
        )

        private val mockColors = listOf(
            "RED", "ORANGE", "YELLOW", "GREEN", "BLUE", "NAVY", "PURPLE", "PINK", "SKY_BLUE", "MINT"
        )
    }
}
