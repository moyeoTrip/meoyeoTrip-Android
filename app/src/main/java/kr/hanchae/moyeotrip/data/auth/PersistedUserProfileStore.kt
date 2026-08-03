package kr.hanchae.moyeotrip.data.auth

import android.content.Context
import java.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.hanchae.moyeotrip.domain.auth.UserDisplayProfile
import kr.hanchae.moyeotrip.domain.auth.UserProfileStore
import org.json.JSONObject

class PersistedUserProfileStore(context: Context) : UserProfileStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(
        UserDisplayProfile(
            nickname = preferences.getString(KEY_NICKNAME, null),
            nicknameColor = preferences.getString(KEY_NICKNAME_COLOR, null),
            profileImageUrl = preferences.getString(KEY_PROFILE_IMAGE_URL, null)
        )
    )
    override val profile: StateFlow<UserDisplayProfile> = state
    override val current: UserDisplayProfile
        get() = state.value

    override fun updateFromAccessToken(accessToken: String) {
        accessToken.nicknameClaim()?.let { nickname ->
            preferences.edit().putString(KEY_NICKNAME, nickname).apply()
            state.value = state.value.copy(nickname = nickname)
        }
    }

    override fun saveNickname(nickname: String, nicknameColor: String?) {
        preferences.edit()
            .putString(KEY_NICKNAME, nickname)
            .putString(KEY_NICKNAME_COLOR, nicknameColor)
            .apply()
        state.value = state.value.copy(nickname = nickname, nicknameColor = nicknameColor)
    }

    override fun saveProfileImage(profileImageUrl: String?) {
        preferences.edit().putString(KEY_PROFILE_IMAGE_URL, profileImageUrl).apply()
        state.value = state.value.copy(profileImageUrl = profileImageUrl)
    }

    override fun clear() {
        preferences.edit().clear().apply()
        state.value = UserDisplayProfile()
    }

    private companion object {
        const val PREFERENCES_NAME = "moyeo_user_display_profile"
        const val KEY_NICKNAME = "nickname"
        const val KEY_NICKNAME_COLOR = "nickname_color"
        const val KEY_PROFILE_IMAGE_URL = "profile_image_url"
    }
}

class InMemoryUserProfileStore : UserProfileStore {
    private val state = MutableStateFlow(UserDisplayProfile())
    override val profile: StateFlow<UserDisplayProfile> = state
    override val current: UserDisplayProfile
        get() = state.value

    override fun updateFromAccessToken(accessToken: String) {
        accessToken.nicknameClaim()?.let { state.value = current.copy(nickname = it) }
    }

    override fun saveNickname(nickname: String, nicknameColor: String?) {
        state.value = current.copy(nickname = nickname, nicknameColor = nicknameColor)
    }

    override fun saveProfileImage(profileImageUrl: String?) {
        state.value = current.copy(profileImageUrl = profileImageUrl)
    }

    override fun clear() {
        state.value = UserDisplayProfile()
    }
}

internal fun String.nicknameClaim(): String? = runCatching {
    val payload = split('.').getOrNull(1) ?: return null
    val decoded = Base64.getUrlDecoder().decode(payload)
    JSONObject(decoded.toString(Charsets.UTF_8)).optString("nickName").takeIf(String::isNotBlank)
}.getOrNull()
