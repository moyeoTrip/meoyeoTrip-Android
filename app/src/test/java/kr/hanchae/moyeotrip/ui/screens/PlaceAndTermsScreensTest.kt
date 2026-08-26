package kr.hanchae.moyeotrip.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceAndTermsScreensTest {
    /** 검색은 서버(`GET tourism-contents?keyword=`)가 한다 — 목록 목데이터는 자리값으로만 남는다. */
    @Test
    fun placeholderIsOnlyUsedForPlansMockContentIds() {
        assertEquals("달기약수터 백숙거리", TourismPlaceCatalog.placeholder("2299341").title)
        // 목데이터에 없는 서버 방문지는 남의 목데이터가 아니라 빈 카드로 시작한다.
        val unknown = TourismPlaceCatalog.placeholder("2017064")
        assertEquals("", unknown.title)
        assertEquals("", unknown.address)
    }

    @Test
    fun onlyRestaurantDetailsExposeMenus() {
        val restaurant = TourismPlaceCatalog.placeholder("2299341")
        val landmark = TourismPlaceCatalog.placeholder("2864117")

        assertEquals(TourismContentType.Food, restaurant.type)
        assertTrue(restaurant.menuNames.isNotEmpty())
        assertFalse(landmark.menuNames.isNotEmpty())
    }

    @Test
    fun termsCatalogContainsFourDocumentsWithRequiredFlags() {
        assertEquals(4, TermsDocument.entries.size)
        assertTrue(TermsDocument.Service.required)
        assertTrue(TermsDocument.Privacy.required)
        assertFalse(TermsDocument.Location.required)
        assertFalse(TermsDocument.Marketing.required)
    }
}
