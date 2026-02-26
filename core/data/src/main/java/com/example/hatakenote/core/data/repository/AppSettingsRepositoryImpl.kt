package com.example.hatakenote.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hatakenote.core.domain.model.AppSettings
import com.example.hatakenote.core.domain.repository.AppSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class AppSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppSettingsRepository {

    private object PreferencesKeys {
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val REMINDER_NOTIFY_DAYS = intPreferencesKey("reminder_notify_days")
    }

    override fun getSettings(): Flow<AppSettings> {
        return context.dataStore.data.map { preferences ->
            AppSettings(
                latitude = preferences[PreferencesKeys.LATITUDE] ?: AppSettings.DEFAULT_LATITUDE,
                longitude = preferences[PreferencesKeys.LONGITUDE] ?: AppSettings.DEFAULT_LONGITUDE,
                locationName = preferences[PreferencesKeys.LOCATION_NAME] ?: AppSettings.DEFAULT_LOCATION_NAME,
                reminderNotifyDaysBefore = preferences[PreferencesKeys.REMINDER_NOTIFY_DAYS] ?: AppSettings.DEFAULT_REMINDER_NOTIFY_DAYS,
            )
        }
    }

    override suspend fun updateLocation(latitude: Double, longitude: Double, locationName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LATITUDE] = latitude
            preferences[PreferencesKeys.LONGITUDE] = longitude
            preferences[PreferencesKeys.LOCATION_NAME] = locationName
        }
    }

    override suspend fun updateReminderNotifyDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_NOTIFY_DAYS] = days
        }
    }
}
