package kr.hanchae.moyeotrip.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 테마 설정 영구 저장.
 * 기존 [kr.hanchae.moyeotrip.data.auth.PersistedUserProfileStore] 와 같은 SharedPreferences 방식이다.
 */
class ThemePreferenceStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(ThemePreference.parse(preferences.getString(KEY_THEME, null)))
    val preference: StateFlow<ThemePreference> = state
    val current: ThemePreference
        get() = state.value

    fun save(preference: ThemePreference) {
        if (preference == state.value) return
        preferences.edit().putString(KEY_THEME, preference.storageValue).apply()
        state.value = preference
    }

    private companion object {
        const val PREFERENCES_NAME = "moyeo_display_settings"
        const val KEY_THEME = "theme_preference"
    }
}
