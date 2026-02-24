package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.ReminderDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAll(): Flow<List<Reminder>> =
        reminderDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getPending(): Flow<List<Reminder>> =
        reminderDao.getPending().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getUpcoming(days: Int): Flow<List<Reminder>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val targetDate = today.plus(days, DateTimeUnit.DAY)
        return reminderDao.getUpcoming(targetDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getById(id: Long): Reminder? =
        reminderDao.getById(id)?.toDomain()

    override fun getByPlantingId(plantingId: Long): Flow<List<Reminder>> =
        reminderDao.getByPlantingId(plantingId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(reminder: Reminder): Long =
        reminderDao.insert(reminder.toEntity())

    override suspend fun update(reminder: Reminder) =
        reminderDao.update(reminder.toEntity())

    override suspend fun markCompleted(id: Long) =
        reminderDao.markCompleted(id)

    override suspend fun delete(reminder: Reminder) =
        reminderDao.delete(reminder.toEntity())

    override suspend fun deleteByPlantingId(plantingId: Long) =
        reminderDao.deleteByPlantingId(plantingId)
}
