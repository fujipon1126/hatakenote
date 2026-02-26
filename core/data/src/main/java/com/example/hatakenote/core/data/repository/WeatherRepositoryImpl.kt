package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.domain.model.DailyForecast
import com.example.hatakenote.core.domain.model.Weather
import com.example.hatakenote.core.domain.model.WeatherCode
import com.example.hatakenote.core.domain.repository.WeatherRepository
import com.example.hatakenote.core.network.api.WeatherApiService
import com.example.hatakenote.core.network.model.WeatherResponse
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
) : WeatherRepository {

    override suspend fun getWeather(latitude: Double, longitude: Double): Result<Weather> {
        return try {
            val response = weatherApiService.getWeather(latitude, longitude)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun WeatherResponse.toDomain(): Weather {
        val dailyForecasts = daily.time.indices.map { index ->
            DailyForecast(
                date = LocalDate.parse(daily.time[index]),
                weatherCode = WeatherCode.fromCode(daily.weatherCode[index]),
                temperatureMax = daily.temperatureMax[index],
                temperatureMin = daily.temperatureMin[index],
                precipitationSum = daily.precipitationSum[index],
            )
        }

        return Weather(
            currentTemperature = currentWeather.temperature,
            currentWeatherCode = WeatherCode.fromCode(currentWeather.weatherCode),
            dailyForecasts = dailyForecasts,
        )
    }
}
