package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * 天気情報
 */
data class Weather(
    val currentTemperature: Double,
    val currentWeatherCode: WeatherCode,
    val dailyForecasts: List<DailyForecast>,
)

/**
 * 日別天気予報
 */
data class DailyForecast(
    val date: LocalDate,
    val weatherCode: WeatherCode,
    val temperatureMax: Double,
    val temperatureMin: Double,
    val precipitationSum: Double,
)

/**
 * WMO Weather interpretation codes
 * https://open-meteo.com/en/docs#weathervariables
 */
enum class WeatherCode(val code: Int, val description: String) {
    CLEAR_SKY(0, "快晴"),
    MAINLY_CLEAR(1, "晴れ"),
    PARTLY_CLOUDY(2, "やや曇り"),
    OVERCAST(3, "曇り"),
    FOG(45, "霧"),
    DEPOSITING_RIME_FOG(48, "着氷性の霧"),
    DRIZZLE_LIGHT(51, "霧雨（弱）"),
    DRIZZLE_MODERATE(53, "霧雨（中）"),
    DRIZZLE_DENSE(55, "霧雨（強）"),
    FREEZING_DRIZZLE_LIGHT(56, "着氷性の霧雨（弱）"),
    FREEZING_DRIZZLE_DENSE(57, "着氷性の霧雨（強）"),
    RAIN_SLIGHT(61, "雨（弱）"),
    RAIN_MODERATE(63, "雨（中）"),
    RAIN_HEAVY(65, "雨（強）"),
    FREEZING_RAIN_LIGHT(66, "着氷性の雨（弱）"),
    FREEZING_RAIN_HEAVY(67, "着氷性の雨（強）"),
    SNOW_SLIGHT(71, "雪（弱）"),
    SNOW_MODERATE(73, "雪（中）"),
    SNOW_HEAVY(75, "雪（強）"),
    SNOW_GRAINS(77, "霧雪"),
    RAIN_SHOWERS_SLIGHT(80, "にわか雨（弱）"),
    RAIN_SHOWERS_MODERATE(81, "にわか雨（中）"),
    RAIN_SHOWERS_VIOLENT(82, "にわか雨（強）"),
    SNOW_SHOWERS_SLIGHT(85, "にわか雪（弱）"),
    SNOW_SHOWERS_HEAVY(86, "にわか雪（強）"),
    THUNDERSTORM(95, "雷雨"),
    THUNDERSTORM_WITH_SLIGHT_HAIL(96, "雷雨（弱い雹）"),
    THUNDERSTORM_WITH_HEAVY_HAIL(99, "雷雨（強い雹）"),
    UNKNOWN(-1, "不明");

    companion object {
        fun fromCode(code: Int): WeatherCode =
            entries.find { it.code == code } ?: UNKNOWN
    }
}
