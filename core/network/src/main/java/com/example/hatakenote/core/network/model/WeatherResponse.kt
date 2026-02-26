package com.example.hatakenote.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    @SerialName("current_weather")
    val currentWeather: CurrentWeather,
    val daily: DailyWeather,
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    @SerialName("windspeed")
    val windSpeed: Double,
    @SerialName("weathercode")
    val weatherCode: Int,
    val time: String,
)

@Serializable
data class DailyWeather(
    val time: List<String>,
    @SerialName("weathercode")
    val weatherCode: List<Int>,
    @SerialName("temperature_2m_max")
    val temperatureMax: List<Double>,
    @SerialName("temperature_2m_min")
    val temperatureMin: List<Double>,
    @SerialName("precipitation_sum")
    val precipitationSum: List<Double>,
)
