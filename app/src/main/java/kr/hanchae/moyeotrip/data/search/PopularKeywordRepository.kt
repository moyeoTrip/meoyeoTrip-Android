package kr.hanchae.moyeotrip.data.search

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.mapObjects
import org.json.JSONObject

/**
 * 12 검색 · 인기 검색어 한 건 — `GET /api/v1/search/popular-keywords` 응답 그대로다.
 *
 * 등락은 서버가 2026-09-09 에 열어줬다(`rankTrend`·`rankChange`). 기준은 **전일 순위**다.
 */
data class PopularKeyword(
    val rank: Int,
    val keyword: String,
    val searchCount: Int,
    /** 전일 순위 대비 등락. 스펙 enum 밖의 값이 오면 `UNKNOWN` 이고 화면이 등락을 그리지 않는다. */
    val rankTrend: RankTrend = RankTrend.UNKNOWN,
    /** 변동 폭. 양수는 상승 · 음수는 하락이고 **신규 진입은 `null`** 이다. */
    val rankChange: Int? = null
)

/** 인기 검색어의 등락 상태 — API 스펙 `PopularSearchKeywordResponse.rankTrend` 의 enum 이다. */
enum class RankTrend {
    UP,
    DOWN,
    SAME,
    NEW,

    /**
     * 서버가 등락을 주지 않았거나 모르는 값이 왔다.
     *
     * 이때는 **등락을 그리지 않는다** — 지어내지 않는다(정본 R1).
     */
    UNKNOWN;

    companion object {
        fun from(raw: String?): RankTrend = entries.firstOrNull { it.name == raw } ?: UNKNOWN
    }
}

interface PopularKeywordRepository {
    /**
     * 인기 검색어 — 모임 검색과 공개 코스 검색을 합산한 집계다.
     *
     * 집계 전이거나 Redis 가 죽으면 빈 목록이 온다. 실패로 다루지 않고 **섹션을 그리지 않는다**.
     */
    suspend fun popularKeywords(limit: Int = 10): List<PopularKeyword>
}

class HttpPopularKeywordRepository(private val client: MoyeoApiClient) : PopularKeywordRepository {
    override suspend fun popularKeywords(limit: Int): List<PopularKeyword> = client
        .getArray("/api/v1/search/popular-keywords?limit=$limit")
        .mapObjects(JSONObject::toPopularKeyword)
}

private fun JSONObject.toPopularKeyword() = PopularKeyword(
    rank = optInt("rank"),
    keyword = optString("keyword"),
    searchCount = optInt("searchCount"),
    rankTrend = RankTrend.from(if (isNull("rankTrend")) null else optString("rankTrend")),
    // 신규 진입은 null 이다 — optInt 의 0 으로 뭉개면 「변동 없음」과 구분되지 않는다.
    rankChange = if (isNull("rankChange")) null else optInt("rankChange")
)
