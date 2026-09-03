package kr.hanchae.moyeotrip.data.search

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.mapObjects
import org.json.JSONObject

/**
 * 12 검색 · 인기 검색어 한 건 — `GET /api/v1/search/popular-keywords` 응답 그대로다.
 *
 * 서버가 주는 값은 이 셋뿐이다. 화면기획에 있는 상승·하락 화살표(`▲`·`—`)는 **목데이터였다** —
 * 순위 변동을 알려주는 필드가 없으므로 그리지 않는다(정본 R1).
 */
data class PopularKeyword(val rank: Int, val keyword: String, val searchCount: Int)

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
    searchCount = optInt("searchCount")
)
