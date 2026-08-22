package kr.hanchae.moyeotrip.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceAndTermsScreensTest {
    @Test
    fun placeSearchFiltersByQueryAndContentType() {
        val food = TourismPlaceCatalog.filtered("달기", TourismContentType.Food)

        assertEquals(listOf("달기약수터 백숙거리"), food.map(TourismPlace::title))
        assertTrue(TourismPlaceCatalog.filtered("청송", null).size >= 4)
        assertTrue(TourismPlaceCatalog.filtered("없는 장소", null).isEmpty())
    }

    @Test
    fun onlyRestaurantDetailsExposeMenus() {
        val restaurant = TourismPlaceCatalog.find("2299341")
        val landmark = TourismPlaceCatalog.find("2864117")

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
