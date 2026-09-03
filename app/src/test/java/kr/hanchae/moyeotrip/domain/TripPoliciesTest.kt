package kr.hanchae.moyeotrip.domain

import kr.hanchae.moyeotrip.data.FeedVisibility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripPoliciesTest {
    @Test
    fun recruitmentSummaryMarksOpenTripsThatMetMinimum() {
        val summary = recruitmentSummary(joined = 4, capacity = 12, minParticipants = 3)

        assertEquals(8, summary.remainingSeats)
        assertEquals(true, summary.minimumMet)
        assertEquals(RecruitmentState.Open, summary.state)
        assertEquals("4/12명", summary.displayText)
        assertEquals("최소 3명 달성", summary.minimumText)
        assertEquals("함께 가기 신청", summary.callToAction)
    }

    @Test
    fun recruitmentSummaryMarksTripsBelowMinimum() {
        val summary = recruitmentSummary(joined = 2, capacity = 5, minParticipants = 3)

        assertEquals(RecruitmentState.BelowMinimum, summary.state)
        assertEquals(false, summary.minimumMet)
        assertEquals(3, summary.remainingSeats)
        assertEquals("최소 3명 필요", summary.minimumText)
        assertEquals("함께 가기 신청", summary.callToAction)
    }

    @Test
    fun recruitmentSummaryMarksFewSeatsAndFullTrips() {
        val fewSeats = recruitmentSummary(joined = 9, capacity = 12, minParticipants = 3)
        val full = recruitmentSummary(joined = 14, capacity = 12, minParticipants = 3)

        assertEquals(RecruitmentState.FewSeats, fewSeats.state)
        assertEquals(3, fewSeats.remainingSeats)
        assertEquals("마감 임박", fewSeats.callToAction)

        assertEquals(RecruitmentState.Full, full.state)
        assertEquals(0, full.remainingSeats)
        assertEquals("12/12명", full.displayText)
        assertEquals("대기 신청", full.callToAction)
    }

    @Test
    fun weatherHeroPolicyDependsOnlyOnTheServerWeather() {
        val clearHero = WeatherHeroPolicy.heroFor(WeatherSignal.Clear)
        val rainyHero = WeatherHeroPolicy.heroFor(WeatherSignal.Rain)

        assertEquals("추천", clearHero.stateLabel)
        assertEquals("맑음", clearHero.weatherLabel)
        assertEquals("경주 첨성대", clearHero.landmark)
        assertEquals("weather_sunny_cheomseongdae", clearHero.lightImageResourceName)
        assertEquals("weather_sunny_cheomseongdae_night", clearHero.darkImageResourceName)
        assertTrue(clearHero.title.contains("어디로 떠나볼까요?"))
        assertEquals("햇살 좋은 날, 걷기 좋은 코스를 추천해드려요", clearHero.subtitle)

        assertEquals("주의", rainyHero.stateLabel)
        assertEquals("비", rainyHero.weatherLabel)
        assertEquals("안동 하회마을", rainyHero.landmark)
        assertEquals("weather_rain_hahoe", rainyHero.imageResourceName(isDark = false))
        assertEquals("weather_rain_hahoe_night", rainyHero.imageResourceName(isDark = true))
        assertTrue(rainyHero.subtitle.contains("우산"))
    }

    @Test
    fun heavyRainHeroUsesAlternativeRecommendationPolicy() {
        val hero = WeatherHeroPolicy.heroFor(WeatherSignal.HeavyRain)

        assertEquals("대체 추천", hero.stateLabel)
        assertEquals("폭우", hero.weatherLabel)
        assertEquals("경주 월정교", hero.landmark)
        assertEquals("weather_heavy_rain_woljeonggyo", hero.imageResourceName(isDark = false))
        assertEquals("weather_heavy_rain_woljeonggyo_night", hero.imageResourceName(isDark = true))
        assertEquals("오늘은 무리하지 말고 실내형 코스를 먼저 확인해보세요", hero.subtitle)
    }

    @Test
    fun weatherHeroPolicyMapsEveryStateToLightAndDarkWebPlanningPngResourceNames() {
        val expectedNames = mapOf(
            WeatherSignal.Clear to ("weather_sunny_cheomseongdae" to "weather_sunny_cheomseongdae_night"),
            WeatherSignal.Cloud to ("weather_cloudy_bulguksa" to "weather_cloudy_bulguksa_night"),
            WeatherSignal.Rain to ("weather_rain_hahoe" to "weather_rain_hahoe_night"),
            WeatherSignal.Snow to ("weather_snow_buseoksa" to "weather_snow_buseoksa_night"),
            WeatherSignal.Fog to ("weather_fog_seokguram" to "weather_fog_seokguram_night"),
            WeatherSignal.Wind to ("weather_wind_homigot" to "weather_wind_homigot_night"),
            WeatherSignal.HeavyRain to ("weather_heavy_rain_woljeonggyo" to "weather_heavy_rain_woljeonggyo_night"),
            WeatherSignal.Heat to ("weather_heatwave_dosan" to "weather_heatwave_dosan_night"),
            WeatherSignal.Dust to ("weather_dust_donggung_wolji" to "weather_dust_donggung_wolji_night")
        )

        expectedNames.forEach { (signal, resourceNames) ->
            val hero = WeatherHeroPolicy.heroFor(signal)

            assertEquals(resourceNames.first, hero.imageResourceName(isDark = false))
            assertEquals(resourceNames.second, hero.imageResourceName(isDark = true))
        }
    }

    @Test
    fun splashAssetPolicyMapsAppearanceToGeneratedPngResourceNames() {
        assertEquals("splash_generated", SplashAssetPolicy.imageResourceName(isDark = false))
        assertEquals("splash_generated_night", SplashAssetPolicy.imageResourceName(isDark = true))
    }

    @Test
    fun applicationNotePolicyKeepsNotionLengthRule() {
        assertEquals(false, ApplicationNotePolicy.isValid("짧음"))
        assertEquals(true, ApplicationNotePolicy.isValid("함께 천천히 걷고 싶어요"))
        assertEquals(200, ApplicationNotePolicy.sanitize("가".repeat(220)).length)
        assertEquals("10자 이상 200자 이하로 남겨요.", ApplicationNotePolicy.helperText("  12345678901  "))
        assertEquals("11/200", ApplicationNotePolicy.counterText("  12345678901  "))
    }

    @Test
    fun feedVisibilityLabelsMatchPlanningDocument() {
        assertEquals("전체공개", FeedVisibility.Public.label)
        assertEquals("친구만", FeedVisibility.Friends.label)
        assertEquals("나만 보기", FeedVisibility.Private.label)
    }
}
