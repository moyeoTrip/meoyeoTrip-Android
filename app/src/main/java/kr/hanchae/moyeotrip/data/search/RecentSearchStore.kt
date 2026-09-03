package kr.hanchae.moyeotrip.data.search

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface RecentSearchStore {
    val keywords: StateFlow<List<String>>

    fun record(keyword: String)

    fun remove(keyword: String)

    fun clear()
}

/**
 * SharedPreferences 영구 저장 — 앱을 다시 켜도 최근 검색어가 남는다.
 * 기존 [kr.hanchae.moyeotrip.data.auth.PersistedUserProfileStore] 와 같은 방식이다.
 */
class PersistedRecentSearchStore(context: Context) : RecentSearchStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(RecentSearchPolicy.decode(preferences.getString(KEY_KEYWORDS, null)))
    override val keywords: StateFlow<List<String>> = state

    override fun record(keyword: String) = write(RecentSearchPolicy.add(state.value, keyword))

    override fun remove(keyword: String) = write(RecentSearchPolicy.remove(state.value, keyword))

    override fun clear() = write(emptyList())

    private fun write(keywords: List<String>) {
        if (keywords == state.value) return
        preferences.edit().putString(KEY_KEYWORDS, RecentSearchPolicy.encode(keywords)).apply()
        state.value = keywords
    }

    private companion object {
        const val PREFERENCES_NAME = "moyeo_recent_searches"
        const val KEY_KEYWORDS = "keywords"
    }
}

/**
 * 기기에 쓰지 않는 저장소. 시험에서 최근 검색어 규칙만 확인할 때 쓴다.
 *
 * 화면은 캡처에서도 [PersistedRecentSearchStore] 를 쓴다 — 캡처 전용으로 예시 검색어를
 * 끼워 넣으면 캡처가 실제 화면과 달라진다.
 */
class InMemoryRecentSearchStore(initialKeywords: List<String> = emptyList()) : RecentSearchStore {
    private val state = MutableStateFlow(initialKeywords)
    override val keywords: StateFlow<List<String>> = state

    override fun record(keyword: String) {
        state.value = RecentSearchPolicy.add(state.value, keyword)
    }

    override fun remove(keyword: String) {
        state.value = RecentSearchPolicy.remove(state.value, keyword)
    }

    override fun clear() {
        state.value = emptyList()
    }
}
