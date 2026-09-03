package kr.hanchae.moyeotrip.data.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecentSearchPolicyTest {
    @Test
    fun addsTrimmedKeywordToTheFront() {
        assertEquals(
            listOf("경주 단풍", "주왕산"),
            RecentSearchPolicy.add(listOf("주왕산"), "  경주 단풍  ")
        )
    }

    @Test
    fun ignoresBlankKeywords() {
        assertEquals(listOf("주왕산"), RecentSearchPolicy.add(listOf("주왕산"), "   "))
        assertEquals(emptyList<String>(), RecentSearchPolicy.add(emptyList(), ""))
    }

    @Test
    fun movesDuplicateToTheFrontInsteadOfAddingItTwice() {
        val result = RecentSearchPolicy.add(listOf("주왕산", "경주", "단풍"), "경주")

        assertEquals(listOf("경주", "주왕산", "단풍"), result)
    }

    @Test
    fun keepsAtMostTenKeywordsAndDropsTheOldest() {
        val filled = (1..RecentSearchPolicy.MAX_ENTRIES).fold(emptyList<String>()) { current, index ->
            RecentSearchPolicy.add(current, "검색어 $index")
        }
        assertEquals(RecentSearchPolicy.MAX_ENTRIES, filled.size)

        val overflowed = RecentSearchPolicy.add(filled, "새 검색어")

        assertEquals(RecentSearchPolicy.MAX_ENTRIES, overflowed.size)
        assertEquals("새 검색어", overflowed.first())
        assertTrue(overflowed.none { it == "검색어 1" })
    }

    @Test
    fun removesOnlyTheRequestedKeyword() {
        assertEquals(
            listOf("주왕산", "단풍"),
            RecentSearchPolicy.remove(listOf("주왕산", "경주", "단풍"), "경주")
        )
    }

    @Test
    fun clearedListSurvivesEncodeAndDecodeRoundTrip() {
        assertEquals("[]", RecentSearchPolicy.encode(emptyList()))
        assertEquals(emptyList<String>(), RecentSearchPolicy.decode("[]"))
        assertEquals(emptyList<String>(), RecentSearchPolicy.decode(null))
    }

    @Test
    fun encodesAndDecodesKeywordOrder() {
        val keywords = listOf("경주", "안동 한옥", "주왕산")

        assertEquals(keywords, RecentSearchPolicy.decode(RecentSearchPolicy.encode(keywords)))
    }

    @Test
    fun decodeSurvivesCorruptedStoredValue() {
        assertEquals(emptyList<String>(), RecentSearchPolicy.decode("not-json"))
    }

    @Test
    fun inMemoryStoreUsedByCaptureKeepsPlanningMockData() {
        val store = InMemoryRecentSearchStore(listOf("경주", "단풍", "황리단길", "안동 한옥", "주왕산"))

        assertEquals(listOf("경주", "단풍", "황리단길", "안동 한옥", "주왕산"), store.keywords.value)

        store.record("포항")
        assertEquals("포항", store.keywords.value.first())

        store.remove("포항")
        assertTrue(store.keywords.value.none { it == "포항" })

        store.clear()
        assertEquals(emptyList<String>(), store.keywords.value)
    }
}
