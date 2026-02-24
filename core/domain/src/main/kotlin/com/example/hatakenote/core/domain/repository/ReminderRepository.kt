package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ReminderRepository {
    fun getAll(): Flow<List<Reminder>>
    fun getPending(): Flow<List<Reminder>>
    fun getUpcoming(days: Int): Flow<List<Reminder>>
    suspend fun getById(id: Long): Reminder?
    fun getByPlantingId(plantingId: Long): Flow<List<Reminder>>
    suspend fun insert(reminder: Reminder): Long
    suspend fun update(reminder: Reminder)
    suspend fun markCompleted(id: Long)
    suspend fun delete(reminder: Reminder)
    suspend fun deleteByPlantingId(plantingId: Long)
}
