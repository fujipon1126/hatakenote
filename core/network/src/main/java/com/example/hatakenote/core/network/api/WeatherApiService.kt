package com.example.hatakenote.core.network.api

import com.example.hatakenote.core.network.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("daily") daily: String = "weathercode,temperature_2m_max,temperature_2m_min,precipitation_sum",
        @Query("timezone") timezone: String = "Asia/Tokyo",
        @Query("forecast_days") forecastDays: Int = 7,
    ): WeatherResponse
}
