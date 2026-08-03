package kr.hanchae.moyeotrip.domain

import kr.hanchae.moyeotrip.data.FeedVisibility
import kr.hanchae.moyeotrip.data.MockTripRepository
import kr.hanchae.moyeotrip.data.TripCourse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TripPoliciesTest {
    @Test
    fun recruitmentSummaryMarksOpenTripsThatMetMinimum() {
        val course = course(participants = 4, capacity = 12)

        val summary = course.recruitmentSummary()

        assertEquals(8, summary.remainingSeats)
        assertEquals(true, summary.minimumMet)
        assertEquals(RecruitmentState.Open, summary.state)
        assertEquals("4/12명", summary.displayText)
        assertEquals("최소 3명 달성", summary.minimumText)
        assertEquals("함께 가기 신청", summary.callToAction)
    }

    @Test
    fun recruitmentSummaryMarksTripsBelowMinimum() {
        val summary = course(participants = 2, capacity = 5).recruitmentSummary()

        assertEquals(RecruitmentState.BelowMinimum, summary.state)
        assertEquals(false, summary.minimumMet)
        assertEquals(3, summary.remainingSeats)
        assertEquals("최소 3명 필요", summary.minimumText)
        assertEquals("함께 가기 신청", summary.callToAction)
    }

    @Test
    fun recruitmentSummaryMarksFewSeatsAndFullTrips() {
        val fewSeats = course(participants = 9, capacity = 12).recruitmentSummary()
        val full = course(participants = 14, capacity = 12).recruitmentSummary()

        assertEquals(RecruitmentState.FewSeats, fewSeats.state)
        assertEquals(3, fewSeats.remainingSeats)
        assertEquals("마감 임박", fewSeats.callToAction)

        assertEquals(RecruitmentState.Full, full.state)
        assertEquals(0, full.remainingSeats)
        assertEquals("12/12명", full.displayText)
        assertEquals("대기 신청", full.callToAction)
    }

    @Test
    fun weatherHeroPolicyUsesWeatherAndFeaturedCourseContext() {
        val course = course(
            title = "하회마을 초록 산책",
            region = "안동",
            participants = 8,
            capacity = 12,
            tags = listOf("고택", "산책", "로컬간식"),
            stops = listOf("하회마을 입구", "부용대 전망", "로컬 찻집")
        )

        val clearHero = WeatherHeroPolicy.heroFor(WeatherSignal.Clear, course)
        val rainyHero = WeatherHeroPolicy.heroFor(WeatherSignal.Rain, course)

        assertEquals("추천", clearHero.stateLabel)
        assertEquals("맑음", clearHero.weatherLabel)
        assertEquals("경주 첨성대", clearHero.landmark)
        assertEquals("weather_sunny_cheomseongdae", clearHero.lightImageResourceName)
        assertEquals("weather_sunny_cheomseongdae_night", clearHero.darkImageResourceName)
        assertTrue(clearHero.title.contains("어디로 떠나볼까요?"))
        assertEquals("햇살 좋은 날, 걷기 좋은 코스를 추천해드려요", clearHero.subtitle)
        assertTrue(clearHero.tags.contains("미세먼지"))

        assertEquals("주의", rainyHero.stateLabel)
        assertEquals("비", rainyHero.weatherLabel)
        assertEquals("안동 하회마을", rainyHero.landmark)
        assertEquals("weather_rain_hahoe", rainyHero.imageResourceName(isDark = false))
        assertEquals("weather_rain_hahoe_night", rainyHero.imageResourceName(isDark = true))
        assertTrue(rainyHero.subtitle.contains("우산"))
    }

    @Test
    fun heavyRainHeroUsesAlternativeRecommendationPolicy() {
        val hero = WeatherHeroPolicy.heroFor(
            signal = WeatherSignal.HeavyRain,
            featuredCourse = course(participants = 3, capacity = 6)
        )

        assertEquals("대체 추천", hero.stateLabel)
        assertEquals("폭우", hero.weatherLabel)
        assertEquals("경주 월정교", hero.landmark)
        assertEquals("weather_heavy_rain_woljeonggyo", hero.imageResourceName(isDark = false))
        assertEquals("weather_heavy_rain_woljeonggyo_night", hero.imageResourceName(isDark = true))
        assertEquals("오늘은 무리하지 말고 실내형 코스를 먼저 확인해보세요", hero.subtitle)
    }

    @Test
    fun weatherCoursePolicyReordersCoursesForUnsafeWeather() {
        val courses = listOf(
            course(id = "cheongsong-juwangsan", participants = 2, capacity = 5),
            course(id = "andong-hahoe", participants = 3, capacity = 6),
            course(id = "gyeongju-healing", participants = 4, capacity = 6)
        )

        val heavyRain = WeatherCoursePolicy.recommendedCourses(WeatherSignal.HeavyRain, courses)
        val wind = WeatherCoursePolicy.recommendedCourses(WeatherSignal.Wind, courses)

        assertEquals("gyeongju-healing", heavyRain.first().id)
        assertEquals("andong-hahoe", wind.first().id)
        assertEquals(courses.map { it.id }.toSet(), heavyRain.map { it.id }.toSet())
    }

    @Test
    fun weatherHeroPolicyMapsEveryStateToLightAndDarkWebPlanningPngResourceNames() {
        val course = course(participants = 3, capacity = 6)
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
            val hero = WeatherHeroPolicy.heroFor(signal, course)

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
        assertEquals("11/200자 · 최소 10자", ApplicationNotePolicy.helperText("  12345678901  "))
    }

    @Test
    fun visibilityAndNicknameRulesMatchPlanningDocument() {
        val nicknamePattern = Regex("^[가-힣]+( [가-힣]+ [0-9]{4})?$")

        assertEquals("전체공개", FeedVisibility.Public.label)
        assertEquals("친구만", FeedVisibility.Friends.label)
        assertEquals("친구에게만", MockTripRepository.dogamVisibility.label)
        assertTrue(MockTripRepository.feedPosts.all { nicknamePattern.matches(it.author) })
        assertTrue(MockTripRepository.chatThreads.all { nicknamePattern.matches(it.partner) })
        assertTrue(MockTripRepository.dogamFriends.all { nicknamePattern.matches(it.nickname) })
    }

    private fun course(
        id: String = "test",
        title: String = "테스트 코스",
        region: String = "안동",
        participants: Int,
        capacity: Int,
        tags: List<String> = listOf("산책", "로컬"),
        stops: List<String> = listOf("출발", "중간", "도착")
    ) = TripCourse(
        id = id,
        title = title,
        region = region,
        oneLine = "테스트용 코스",
        imageEmoji = "*",
        duration = "당일",
        courseTime = "2시간",
        distance = "4.2km",
        recommendedSeason = "사계절",
        price = "0원",
        host = "테스터",
        hostAvatar = "*",
        participants = participants,
        capacity = capacity,
        deadlineLabel = "마감 D-1",
        startLabel = "2026.06.06 (토)",
        meetingPoint = "테스트역",
        rating = 4.8,
        tags = tags,
        stops = stops,
        recruitmentNote = "테스트 모집 안내"
    )
}
