package com.example.hatakenote.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hatakenote.core.domain.repository.EntityLastViewedRepository
import com.example.hatakenote.core.domain.repository.ViewedEntityType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject

private val Context.entityViewStateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "entity_view_state",
)

class EntityLastViewedRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : EntityLastViewedRepository {

    private val serializer = MapSerializer(Long.serializer(), Long.serializer())

    private fun keyFor(type: ViewedEntityType) = when (type) {
        ViewedEntityType.PLANTING -> stringPreferencesKey("planting_last_viewed_map")
        ViewedEntityType.WORK_LOG -> stringPreferencesKey("worklog_last_viewed_map")
        ViewedEntityType.HARVEST -> stringPreferencesKey("harvest_last_viewed_map")
    }

    override fun lastViewed(type: ViewedEntityType): Flow<Map<Long, Instant>> {
        val key = keyFor(type)
        return context.entityViewStateDataStore.data.map { preferences ->
            decodeMap(preferences[key])
        }
    }

    override suspend fun markViewed(type: ViewedEntityType, id: Long) {
        markManyViewed(type, listOf(id))
    }

    override suspend fun markManyViewed(type: ViewedEntityType, ids: Collection<Long>) {
        if (ids.isEmpty()) return
        val now = Clock.System.now().toEpochMilliseconds()
        val key = keyFor(type)
        context.entityViewStateDataStore.edit { preferences ->
            val current = decodeMillisMap(preferences[key]).toMutableMap()
            for (id in ids) {
                current[id] = now
            }
            preferences[key] = Json.encodeToString(serializer, current)
        }
    }

    private fun decodeMap(raw: String?): Map<Long, Instant> {
        return decodeMillisMap(raw).mapValues { Instant.fromEpochMilliseconds(it.value) }
    }

    private fun decodeMillisMap(raw: String?): Map<Long, Long> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching { Json.decodeFromString(serializer, raw) }.getOrDefault(emptyMap())
    }
}
