package kr.hanchae.moyeotrip.domain

enum class RecruitmentState {
    BelowMinimum,
    Open,
    FewSeats,
    Full
}

data class RecruitmentSummary(
    val remainingSeats: Int,
    val minimumMet: Boolean,
    val progress: Float,
    val state: RecruitmentState,
    val displayText: String,
    val minimumText: String,
    val callToAction: String
)

/**
 * 모집 인원 요약. 값은 전부 서버 모집(chat-rooms)에서 온 숫자다 —
 * 최소 인원을 서버가 주지 않으면 [minParticipants] 를 0 으로 넘겨 "최소 인원" 문구가 빠지게 둔다.
 */
fun recruitmentSummary(joined: Int, capacity: Int, minParticipants: Int): RecruitmentSummary {
    val remainingSeats = (capacity - joined).coerceAtLeast(0)
    val shownParticipants = joined.coerceAtMost(capacity)
    val minimumMet = joined >= minParticipants
    val state = when {
        remainingSeats == 0 -> RecruitmentState.Full
        !minimumMet -> RecruitmentState.BelowMinimum
        remainingSeats <= 3 -> RecruitmentState.FewSeats
        else -> RecruitmentState.Open
    }
    val callToAction = when (state) {
        RecruitmentState.BelowMinimum -> "함께 가기 신청"
        RecruitmentState.Open -> "함께 가기 신청"
        RecruitmentState.FewSeats -> "마감 임박"
        RecruitmentState.Full -> "대기 신청"
    }

    return RecruitmentSummary(
        remainingSeats = remainingSeats,
        minimumMet = minimumMet,
        progress = if (capacity <= 0) 0f else (shownParticipants.toFloat() / capacity.toFloat()).coerceIn(0f, 1f),
        state = state,
        displayText = "$shownParticipants/${capacity}명",
        minimumText = "최소 ${minParticipants}명 ${if (minimumMet) "달성" else "필요"}",
        callToAction = callToAction
    )
}

enum class WeatherSignal {
    Clear,
    Cloud,
    Rain,
    Snow,
    Fog,
    Wind,
    HeavyRain,
    Heat,
    Dust
}

enum class WeatherHeroState {
    Good,
    Caution,
    Blocked
}

data class WeatherHero(
    val state: WeatherHeroState,
    val stateLabel: String,
    val weatherLabel: String,
    val landmark: String,
    val lightImageResourceName: String,
    val darkImageResourceName: String,
    val title: String,
    val subtitle: String,
    val tags: List<String>
) {
    fun imageResourceName(isDark: Boolean): String = if (isDark) darkImageResourceName else lightImageResourceName
}

object WeatherHeroPolicy {
    private const val DEFAULT_TITLE = "이번 주말,\n어디로 떠나볼까요?"

    fun heroFor(signal: WeatherSignal): WeatherHero {
        val routeMood = when (signal) {
            WeatherSignal.Clear -> RouteMood(
                state = WeatherHeroState.Good,
                stateLabel = "추천",
                weatherLabel = "맑음",
                landmark = "경주 첨성대",
                lightImageResourceName = "weather_sunny_cheomseongdae",
                darkImageResourceName = "weather_sunny_cheomseongdae_night",
                title = DEFAULT_TITLE,
                subtitle = "햇살 좋은 날, 걷기 좋은 코스를 추천해드려요"
            )

            WeatherSignal.Cloud -> RouteMood(
                state = WeatherHeroState.Good,
                stateLabel = "추천",
                weatherLabel = "구름",
                landmark = "경주 불국사",
                lightImageResourceName = "weather_cloudy_bulguksa",
                darkImageResourceName = "weather_cloudy_bulguksa_night",
                title = DEFAULT_TITLE,
                subtitle = "선선한 날씨에 역사 산책 코스를 추천해드려요"
            )

            WeatherSignal.Rain -> RouteMood(
                state = WeatherHeroState.Caution,
                stateLabel = "주의",
                weatherLabel = "비",
                landmark = "안동 하회마을",
                lightImageResourceName = "weather_rain_hahoe",
                darkImageResourceName = "weather_rain_hahoe_night",
                title = DEFAULT_TITLE,
                subtitle = "우산과 실내 동선을 챙겨 여유로운 코스를 골라드려요"
            )

            WeatherSignal.Snow -> RouteMood(
                state = WeatherHeroState.Caution,
                stateLabel = "주의",
                weatherLabel = "눈",
                landmark = "영주 부석사",
                lightImageResourceName = "weather_snow_buseoksa",
                darkImageResourceName = "weather_snow_buseoksa_night",
                title = DEFAULT_TITLE,
                subtitle = "눈길 이동이 짧고 쉬어가기 좋은 코스를 먼저 보여드려요"
            )

            WeatherSignal.Fog -> RouteMood(
                state = WeatherHeroState.Caution,
                stateLabel = "주의",
                weatherLabel = "안개",
                landmark = "경주 석굴암",
                lightImageResourceName = "weather_fog_seokguram",
                darkImageResourceName = "weather_fog_seokguram_night",
                title = DEFAULT_TITLE,
                subtitle = "시야가 흐린 날엔 가까운 코스와 안전한 이동을 우선해요"
            )

            WeatherSignal.Wind -> RouteMood(
                state = WeatherHeroState.Blocked,
                stateLabel = "대체 추천",
                weatherLabel = "강풍",
                landmark = "포항 호미곶",
                lightImageResourceName = "weather_wind_homigot",
                darkImageResourceName = "weather_wind_homigot_night",
                title = DEFAULT_TITLE,
                subtitle = "바람이 강한 날엔 해안 코스 대신 대체 코스를 추천해요"
            )

            WeatherSignal.HeavyRain -> RouteMood(
                state = WeatherHeroState.Blocked,
                stateLabel = "대체 추천",
                weatherLabel = "폭우",
                landmark = "경주 월정교",
                lightImageResourceName = "weather_heavy_rain_woljeonggyo",
                darkImageResourceName = "weather_heavy_rain_woljeonggyo_night",
                title = DEFAULT_TITLE,
                subtitle = "오늘은 무리하지 말고 실내형 코스를 먼저 확인해보세요"
            )

            WeatherSignal.Heat -> RouteMood(
                state = WeatherHeroState.Blocked,
                stateLabel = "대체 추천",
                weatherLabel = "폭염",
                landmark = "안동 도산서원",
                lightImageResourceName = "weather_heatwave_dosan",
                darkImageResourceName = "weather_heatwave_dosan_night",
                title = DEFAULT_TITLE,
                subtitle = "더위가 심한 날엔 짧은 동선과 그늘 많은 장소를 추천해요"
            )

            WeatherSignal.Dust -> RouteMood(
                state = WeatherHeroState.Blocked,
                stateLabel = "대체 추천",
                weatherLabel = "미세먼지",
                landmark = "경주 동궁과 월지",
                lightImageResourceName = "weather_dust_donggung_wolji",
                darkImageResourceName = "weather_dust_donggung_wolji_night",
                title = DEFAULT_TITLE,
                subtitle = "공기가 탁한 날엔 실내 휴식과 짧은 이동 코스를 우선해요"
            )
        }

        return WeatherHero(
            state = routeMood.state,
            stateLabel = routeMood.stateLabel,
            weatherLabel = routeMood.weatherLabel,
            landmark = routeMood.landmark,
            lightImageResourceName = routeMood.lightImageResourceName,
            darkImageResourceName = routeMood.darkImageResourceName,
            title = routeMood.title,
            subtitle = routeMood.subtitle,
            tags = listOf("맑음", "구름", "비", "눈", "안개", "강풍", "폭우", "폭염", "미세먼지")
        )
    }
}

object ApplicationNotePolicy {
    const val MIN_LENGTH = 10
    const val MAX_LENGTH = 200

    /**
     * 저장·전송 직전에 다듬는다. **입력 중에는 쓰지 마라** —
     * 키 입력마다 trim 하면 끝에 친 공백과 줄바꿈이 그 자리에서 지워져
     * 사용자에게는 스페이스·엔터가 아예 안 먹는 것처럼 보인다.
     */
    fun sanitize(input: String): String = input.trim().take(MAX_LENGTH)

    /** 입력 중 길이 제한. 공백·줄바꿈은 건드리지 않는다. */
    fun clampWhileTyping(input: String): String = input.take(MAX_LENGTH)

    fun isValid(input: String): Boolean = sanitize(input).length in MIN_LENGTH..MAX_LENGTH

    // 화면기획·웹·iOS와 같은 문장 + 오른쪽 글자 수 카운터
    fun helperText(input: String): String = "${MIN_LENGTH}자 이상 ${MAX_LENGTH}자 이하로 남겨요."

    fun counterText(input: String): String = "${sanitize(input).length}/$MAX_LENGTH"
}

object SplashAssetPolicy {
    const val LIGHT_IMAGE_RESOURCE_NAME = "splash_generated"
    const val DARK_IMAGE_RESOURCE_NAME = "splash_generated_night"

    fun imageResourceName(isDark: Boolean): String = if (isDark) DARK_IMAGE_RESOURCE_NAME else LIGHT_IMAGE_RESOURCE_NAME
}

private data class RouteMood(
    val state: WeatherHeroState,
    val stateLabel: String,
    val weatherLabel: String,
    val landmark: String,
    val lightImageResourceName: String,
    val darkImageResourceName: String,
    val title: String,
    val subtitle: String
)
