package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.WorkLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface WorkLogRepository {
    fun getAll(): Flow<List<WorkLog>>
    suspend fun getById(id: Long): WorkLog?
    fun getByPlotId(plotId: Long): Flow<List<WorkLog>>
    fun getByPlantingId(plantingId: Long): Flow<List<WorkLog>>
    fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkLog>>
    fun getByDate(date: LocalDate): Flow<List<WorkLog>>
    suspend fun insert(workLog: WorkLog): Long
    suspend fun update(workLog: WorkLog)
    suspend fun delete(workLog: WorkLog)
}
