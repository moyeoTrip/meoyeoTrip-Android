package kr.hanchae.moyeotrip.domain.auth

const val NICKNAME_CANDIDATE_ENDPOINT = "/api/v1/auth/nickname-candidates"

data class NicknameCandidate(
    val nickname: String,
    val adjective: String = nickname.substringBefore(' '),
    val animal: String = nickname.split(' ').dropLast(1).lastOrNull().orEmpty(),
    val color: String = "MINT",
    val description: String = "함께 천천히 경북을 둘러보는 여행자예요"
)

data class NicknameCandidateResponse(val selectionToken: String, val candidates: List<NicknameCandidate>)

/**
 * 이름 후보는 **서버만** 준다 (`POST /api/v1/auth/nickname-candidates`).
 * 앱이 후보를 만들어 두면 가입 화면이 서버가 죽었을 때도 멀쩡해 보인다 — 그 순간을 감추지 않는다.
 */
fun interface NicknameCandidateGateway {
    suspend fun fetchCandidates(): NicknameCandidateResponse
}
