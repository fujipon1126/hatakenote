package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.FertilizerSchedule
import kotlinx.coroutines.flow.Flow

interface FertilizerScheduleRepository {
    fun getAll(): Flow<List<FertilizerSchedule>>
    suspend fun getById(id: Long): FertilizerSchedule?
    fun getByCropId(cropId: Long): Flow<List<FertilizerSchedule>>
    suspend fun insert(schedule: FertilizerSchedule): Long
    suspend fun update(schedule: FertilizerSchedule)
    suspend fun delete(schedule: FertilizerSchedule)
}
