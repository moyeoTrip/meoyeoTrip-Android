package kr.hanchae.moyeotrip.data.weather

import kr.hanchae.moyeotrip.data.api.MoyeoApiClient
import kr.hanchae.moyeotrip.data.api.stringOrNull
import kr.hanchae.moyeotrip.domain.WeatherSignal
import org.json.JSONObject

/**
 * 홈(09) 히어로가 쓰는 경북 현재 날씨.
 * [locationName] 은 히어로 이미지 위 라벨에 그대로 붙는다 — 클라가 지명을 만들어 쓰지 않는다.
 */
data class GyeongbukWeather(val signal: WeatherSignal, val locationName: String?)

interface WeatherRepository {
    suspend fun gyeongbuk(): GyeongbukWeather
}

class HttpWeatherRepository(private val client: MoyeoApiClient) : WeatherRepository {
    override suspend fun gyeongbuk(): GyeongbukWeather =
        client.getObject("/api/v1/weather/gyeongbuk").toGyeongbukWeather()
}

private fun JSONObject.toGyeongbukWeather(): GyeongbukWeather = GyeongbukWeather(
    signal = stringOrNull("condition").toWeatherSignal(),
    locationName = stringOrNull("locationName")
)

/** 서버 `GyeongbukWeatherCondition` 9종을 그대로 받는다. 모르는 값이면 히어로를 그리지 않는다. */
private fun String?.toWeatherSignal(): WeatherSignal = when (this) {
    "SUNNY" -> WeatherSignal.Clear
    "CLOUDY" -> WeatherSignal.Cloud
    "RAIN" -> WeatherSignal.Rain
    "SNOW" -> WeatherSignal.Snow
    "FOG" -> WeatherSignal.Fog
    "STRONG_WIND" -> WeatherSignal.Wind
    "HEAVY_RAIN" -> WeatherSignal.HeavyRain
    "HEAT_WAVE" -> WeatherSignal.Heat
    "FINE_DUST" -> WeatherSignal.Dust
    else -> throw IllegalArgumentException("알 수 없는 날씨 상태: $this")
}
