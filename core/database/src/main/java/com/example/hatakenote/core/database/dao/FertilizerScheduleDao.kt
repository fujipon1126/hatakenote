package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hatakenote.core.database.entity.FertilizerScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerScheduleDao {
    @Query("SELECT * FROM fertilizer_schedules ORDER BY cropId, daysAfterPlanting")
    fun getAll(): Flow<List<FertilizerScheduleEntity>>

    @Query("SELECT * FROM fertilizer_schedules WHERE id = :id")
    suspend fun getById(id: Long): FertilizerScheduleEntity?

    @Query("SELECT * FROM fertilizer_schedules WHERE cropId = :cropId ORDER BY daysAfterPlanting")
    fun getByCropId(cropId: Long): Flow<List<FertilizerScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: FertilizerScheduleEntity): Long

    @Update
    suspend fun update(schedule: FertilizerScheduleEntity)

    @Delete
    suspend fun delete(schedule: FertilizerScheduleEntity)
}
