package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeather(latitude: Double, longitude: Double): Result<Weather>
}
