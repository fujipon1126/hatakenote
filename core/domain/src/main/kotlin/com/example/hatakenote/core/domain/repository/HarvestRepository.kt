package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Harvest
import kotlinx.coroutines.flow.Flow

interface HarvestRepository {
    fun getByPlantingId(plantingId: Long): Flow<List<Harvest>>
    suspend fun insert(harvest: Harvest): Long
    suspend fun delete(harvest: Harvest)
}
