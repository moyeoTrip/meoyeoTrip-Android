package kr.hanchae.moyeotrip.domain.auth

import kotlinx.coroutines.flow.StateFlow

enum class AuthProvider(val pathValue: String) {
    EMAIL("email"),
    GOOGLE("google"),
    KAKAO("kakao"),
    APPLE("apple")
}

enum class EmailAuthAction {
    SIGN_IN,
    CREATE_ACCOUNT
}

data class EmailAuthRequest(val email: String, val password: String, val action: EmailAuthAction)

enum class SignupState {
    USER_INFO_REQUIRED,
    PROFILE_IMAGE_REQUIRED,
    SIGNUP_COMPLETE
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
    val birthDate: String
)

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
