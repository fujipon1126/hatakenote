package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hatakenote.core.database.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY scheduledDate")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY scheduledDate")
    fun getPending(): Flow<List<ReminderEntity>>

    @Query("""
        SELECT * FROM reminders
        WHERE isCompleted = 0 AND scheduledDate <= :targetDate
        ORDER BY scheduledDate
    """)
    fun getUpcoming(targetDate: LocalDate): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE plantingId = :plantingId ORDER BY scheduledDate")
    fun getByPlantingId(plantingId: Long): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity): Long

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE plantingId = :plantingId")
    suspend fun deleteByPlantingId(plantingId: Long)
}
