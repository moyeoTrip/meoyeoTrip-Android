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
 * 번호별 비교 캡처(`moyeo_screen`)에서 쓰는 저장소 — 기기에 남은 검색 기록이 캡처에 섞이면
 * 화면기획과 픽셀이 어긋나므로 목데이터를 메모리에만 들고 있는다.
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

/** 화면기획 12 검색의 최근 검색어 목데이터 — 캡처에서만 쓴다. */
val PLANNING_RECENT_SEARCHES = listOf("경주", "단풍", "황리단길", "안동 한옥", "주왕산")
