package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.hatakenote.core.database.entity.WorkLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface WorkLogDao {
    @Query("SELECT * FROM work_logs ORDER BY workDate DESC")
    fun getAll(): Flow<List<WorkLogEntity>>

    @Query("SELECT * FROM work_logs WHERE id = :id")
    suspend fun getById(id: Long): WorkLogEntity?

    @Query("SELECT * FROM work_logs WHERE plotId = :plotId ORDER BY workDate DESC")
    fun getByPlotId(plotId: Long): Flow<List<WorkLogEntity>>

    @Query("SELECT * FROM work_logs WHERE plantingId = :plantingId ORDER BY workDate DESC")
    fun getByPlantingId(plantingId: Long): Flow<List<WorkLogEntity>>

    @Query("SELECT * FROM work_logs WHERE workDate BETWEEN :startDate AND :endDate ORDER BY workDate DESC")
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkLogEntity>>

    @Query("SELECT * FROM work_logs WHERE workDate = :date ORDER BY id DESC")
    fun getByDate(date: LocalDate): Flow<List<WorkLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workLog: WorkLogEntity): Long

    @Update
    suspend fun update(workLog: WorkLogEntity)

    @Delete
    suspend fun delete(workLog: WorkLogEntity)
}
