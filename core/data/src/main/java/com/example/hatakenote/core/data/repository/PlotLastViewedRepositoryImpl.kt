package com.example.hatakenote.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hatakenote.core.domain.repository.PlotLastViewedRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

private val Context.plotViewStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "plot_view_state",
)

class PlotLastViewedRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PlotLastViewedRepository {

    private val mapKey = stringPreferencesKey("plot_last_viewed_map")
    private val serializer = MapSerializer(Long.serializer(), Long.serializer())

    override fun lastViewedMap(): Flow<Map<Long, Instant>> {
        return context.plotViewStateDataStore.data.map { preferences ->
            decodeMap(preferences[mapKey])
        }
    }

    override suspend fun markViewed(plotId: Long) {
        val now = Clock.System.now().toEpochMilliseconds()
        context.plotViewStateDataStore.edit { preferences ->
            val current = decodeMap(preferences[mapKey]).mapValues { it.value.toEpochMilliseconds() }
            val updated = current.toMutableMap().apply { this[plotId] = now }
            preferences[mapKey] = Json.encodeToString(serializer, updated)
        }
    }

    private fun decodeMap(raw: String?): Map<Long, Instant> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            Json.decodeFromString(serializer, raw)
                .mapValues { Instant.fromEpochMilliseconds(it.value) }
        }.getOrDefault(emptyMap())
    }
}
