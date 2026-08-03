package kr.hanchae.moyeotrip.ui.screens

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BirthDateValidationTest {
    private val today = LocalDate.of(2026, 8, 1)

    @Test
    fun acceptsRealPastDateInIsoFormat() {
        assertTrue(isValidBirthDate("1998-04-12", today))
    }

    @Test
    fun rejectsMalformedImpossibleAndFutureDates() {
        assertFalse(isValidBirthDate("1998/04/12", today))
        assertFalse(isValidBirthDate("2025-02-29", today))
        assertFalse(isValidBirthDate("2027-01-01", today))
    }
}
