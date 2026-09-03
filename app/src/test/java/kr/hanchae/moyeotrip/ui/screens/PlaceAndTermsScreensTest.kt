package kr.hanchae.moyeotrip.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceAndTermsScreensTest {
    /** 상세를 받기 전 자리값은 **빈 카드**다 — 예시 방문지를 잠깐이라도 그리지 않는다. */
    @Test
    fun placeholderStartsEmptyForEveryContentId() {
        listOf("2299341", "2864117", "2017064").forEach { contentId ->
            val place = emptyTourismPlace(contentId)

            assertEquals(contentId, place.contentId)
            assertEquals("", place.title)
            assertEquals("", place.address)
            assertEquals(0.0, place.latitude, 0.0)
            assertTrue(place.menuNames.isEmpty())
            assertTrue(place.photoUrls.isEmpty())
        }
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
