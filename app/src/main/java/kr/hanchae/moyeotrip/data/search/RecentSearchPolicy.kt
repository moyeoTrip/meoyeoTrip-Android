package kr.hanchae.moyeotrip.data.search

import org.json.JSONArray

/**
 * 최근 검색어 정책 — 트림 · 빈 값 무시 · 중복 최신순 승격 · 최대 10개 · 저장값 sanitize.
 *
 * 저장 매체와 무관한 순수 로직이라 단위 테스트로 검증한다. 세 플랫폼이 같은 규칙을 쓴다
 * (iOS `RecentSearchPolicy`). 서버에 최근 검색어 API가 없어(클라이언트 전용 기능) 기기에만 남는다.
 */
object RecentSearchPolicy {
    /** 화면기획에 목록 상한이 명시되지 않아 클라이언트 정책으로 10개를 쓴다. */
    const val MAX_ENTRIES = 10

    /** 공백을 다듬어 맨 앞에 넣고, 이미 있으면 최신순으로 끌어올린다. 빈 문자열은 무시한다. */
    fun add(current: List<String>, keyword: String): List<String> {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return current
        return (listOf(trimmed) + current.filterNot { it.equals(trimmed, ignoreCase = true) })
            .take(MAX_ENTRIES)
    }

    fun remove(current: List<String>, keyword: String): List<String> = current.filterNot { it == keyword }

    fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length())
                .mapNotNull { array.optString(it).takeIf(String::isNotBlank) }
                .take(MAX_ENTRIES)
        }.getOrDefault(emptyList())
    }

    fun encode(keywords: List<String>): String = JSONArray().apply {
        keywords.forEach(::put)
    }.toString()
}
