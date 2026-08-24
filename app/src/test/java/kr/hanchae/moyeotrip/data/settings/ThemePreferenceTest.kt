package kr.hanchae.moyeotrip.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePreferenceTest {
    @Test
    fun parsesThreeStoredStatesAndFallsBackToSystem() {
        assertEquals(ThemePreference.System, ThemePreference.parse("system"))
        assertEquals(ThemePreference.Light, ThemePreference.parse("light"))
        assertEquals(ThemePreference.Dark, ThemePreference.parse("dark"))
        assertEquals(ThemePreference.System, ThemePreference.parse(null))
        assertEquals(ThemePreference.System, ThemePreference.parse(""))
        assertEquals(ThemePreference.System, ThemePreference.parse("solarized"))
    }

    @Test
    fun rowValuesMatchTheScreenPlanningLabels() {
        assertEquals("시스템 기본", ThemePreference.System.label)
        assertEquals("라이트", ThemePreference.Light.label)
        assertEquals("다크", ThemePreference.Dark.label)
    }

    @Test
    fun tappingTheRowCyclesSystemLightDarkAndBack() {
        assertEquals(ThemePreference.Light, ThemePreference.System.next())
        assertEquals(ThemePreference.Dark, ThemePreference.Light.next())
        assertEquals(ThemePreference.System, ThemePreference.Dark.next())
    }

    @Test
    fun systemPreferenceFollowsTheOperatingSystem() {
        assertTrue(resolveDarkTheme(null, ThemePreference.System, systemDarkTheme = true))
        assertFalse(resolveDarkTheme(null, ThemePreference.System, systemDarkTheme = false))
    }

    @Test
    fun explicitPreferenceIgnoresTheOperatingSystem() {
        assertFalse(resolveDarkTheme(null, ThemePreference.Light, systemDarkTheme = true))
        assertTrue(resolveDarkTheme(null, ThemePreference.Dark, systemDarkTheme = false))
    }

    @Test
    fun captureForcedThemeAlwaysWinsOverUserPreference() {
        assertFalse(resolveDarkTheme(false, ThemePreference.Dark, systemDarkTheme = true))
        assertTrue(resolveDarkTheme(true, ThemePreference.Light, systemDarkTheme = false))
    }
}
