package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.flow.StateFlow

enum class AuthProvider(val pathValue: String) {
    EMAIL("email"),
    GOOGLE("google"),
    KAKAO("kakao"),
    APPLE("apple")
}

/**
 * 이메일 인증 요청.
 *
 * 로그인/가입을 나누지 않는다 — 한 번 로그인해 보고 계정이 없으면 그대로 만든다
 * (`FirebaseIdentityTokenProvider.signInOrCreate`).
 */
data class EmailAuthRequest(val email: String, val password: String)

enum class SignupState {
    USER_INFO_REQUIRED,
    PROFILE_IMAGE_REQUIRED,
    SIGNUP_COMPLETE
}

/**
 * 일반 API 가 409 로 돌려주는 "이 사용자는 아직 가입이 안 끝났다" 신호.
 *
 * **토큰 문제가 아니다.** 재발급 경로로 흘려보내면 재발급 → 같은 409 → 재발급으로 무한 재시도가 된다.
 * 그래서 상태 코드가 아니라 **오류 코드만으로** 판정한다 — 서버가 401 로 감싸 보내더라도
 * 재발급보다 이 판정이 먼저 걸려야 한다(정본 R1).
 */
enum class SignupGateStage(val errorCode: Int) {
    /** 닉네임·성별·생년월일이 아직 없다 → 회원 정보 입력 단계. */
    USER_INFO(40902),

    /** 프로필 이미지를 아직 고르지 않았다 → 프로필 이미지 생성·선택 단계. */
    PROFILE_IMAGE(40918);

    companion object {
        /**
         * 프로필 이미지 API 3종에만 나오는 "이미 설정을 마쳤다" 코드.
         * 가입 게이트와 반대 방향이라 [SignupGateStage] 에 넣지 않는다 —
         * 오류로 띄우지 않고 조용히 다음 화면으로 넘긴다(정본 R2).
         */
        const val PROFILE_IMAGE_ALREADY_SET_CODE = 40919

        fun ofCode(errorCode: Int?): SignupGateStage? = entries.firstOrNull { it.errorCode == errorCode }
    }
}

enum class Gender(val apiValue: String) {
    FEMALE("F"),
    MALE("M"),
    UNDISCLOSED("N")
}

data class SignupInput(
    val nicknameSelectionToken: String,
    val nickname: String,
    val gender: Gender,
    val birthDate: String,
    /**
     * 사용자가 동의한 **서버 약관의 ID**. 서버 필수 항목이다.
     *
     * 빠지면 서버가 400 `40012` "필수 약관에 모두 동의해야 회원가입할 수 있습니다." 로 막는다.
     * 목록은 `GET /api/v1/terms` 가 주는 것을 그대로 쓴다 — 클라가 약관을 따로 갖지 않는다.
     */
    val agreedTermIds: List<Long>,
    /**
     * 07 취향 단계에서 고른 **서버 여행 스타일 id** (`GET /api/v1/users/me/profile/options` 의 `travelStyles`).
     *
     * 지금까지는 고른 값이 어디로도 전송되지 않아 조용히 유실됐다. 선택 항목이라 비어 있으면 아예 싣지 않는다.
     * 잘못된 id 는 서버가 `40015` 로 거절한다.
     */
    val travelStyleIds: List<Long> = emptyList(),
    /**
     * 07 취향 단계에서 고른 **서버 관심 지역 id** (`profile/options` 의 `interestedRegions`). 경북 시·군만 허용된다.
     * 잘못된 id 는 서버가 `40014` 로 거절한다.
     */
    val interestedRegionIds: List<Long> = emptyList()
)

/**
 * 사용자가 소셜 로그인 화면에서 **스스로 그만둔 것**. 실패가 아니다.
 *
 * SDK 오류를 그대로 흘려보내면 화면이 `user canceled` 같은 개발자 언어를 띄운다.
 * 취소를 이 형으로 갈라내 호출부가 "조용히 아무 일도 하지 않음"을 고를 수 있게 한다(정본 R1·R2).
 * 코루틴 취소와 섞이면 안 되므로 `CancellationException` 을 상속하지 않는다.
 */
class SocialLoginCancelledException(cause: Throwable? = null) : Exception("소셜 로그인을 취소했어요.", cause)

data class IdentityToken(val provider: AuthProvider, val idToken: String, val fcmToken: String? = null)

data class LoginResult(
    val accessToken: String?,
    val refreshToken: String?,
    val isNewUser: Boolean,
    val signupState: SignupState,
    val providerType: AuthProvider
)

data class ServiceSession(val accessToken: String, val refreshToken: String, val signupState: SignupState)

data class ProfileImageCandidate(val profileImageId: Long, val profileImageUrl: String, val selected: Boolean)

data class ProfileImageCandidates(
    val candidates: List<ProfileImageCandidate>,
    val generationCount: Int,
    val remainingGenerationCount: Int,
    val signupState: SignupState
)

data class ProfileImageSelectionResult(val selectedImage: ProfileImageCandidate, val signupState: SignupState)

data class UserDisplayProfile(
    val nickname: String? = null,
    val nicknameColor: String? = null,
    val profileImageUrl: String? = null
)

interface UserProfileStore {
    val profile: StateFlow<UserDisplayProfile>

    val current: UserDisplayProfile

    fun updateFromAccessToken(accessToken: String)

    fun saveNickname(nickname: String, nicknameColor: String?)

    fun saveProfileImage(profileImageUrl: String?)

    fun clear()
}

data class AuthSessionState(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val signupState: SignupState? = null,
    val provider: AuthProvider? = null
) {
    val isAuthenticated: Boolean
        get() = !accessToken.isNullOrBlank() && signupState == SignupState.SIGNUP_COMPLETE
}

interface AuthSessionStore {
    val current: AuthSessionState

    fun saveLogin(provider: AuthProvider, result: LoginResult)

    fun saveSignup(provider: AuthProvider, session: ServiceSession)

    fun completeProfile()

    fun clear()
}

class InMemoryAuthSessionStore : AuthSessionStore {
    override var current: AuthSessionState = AuthSessionState()
        private set

    override fun saveLogin(provider: AuthProvider, result: LoginResult) {
        current = AuthSessionState(
            accessToken = result.accessToken,
            refreshToken = result.refreshToken,
            signupState = result.signupState,
            provider = provider
        )
    }

    override fun saveSignup(provider: AuthProvider, session: ServiceSession) {
        current = AuthSessionState(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken,
            signupState = session.signupState,
            provider = provider
        )
    }

    override fun completeProfile() {
        current = current.copy(signupState = SignupState.SIGNUP_COMPLETE)
    }

    override fun clear() {
        current = AuthSessionState()
    }
}
