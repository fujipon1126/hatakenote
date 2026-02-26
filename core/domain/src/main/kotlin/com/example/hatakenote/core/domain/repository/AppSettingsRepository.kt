package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateLocation(latitude: Double, longitude: Double, locationName: String)
    suspend fun updateReminderNotifyDays(days: Int)
}
