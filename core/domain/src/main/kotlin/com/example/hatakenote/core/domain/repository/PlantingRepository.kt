package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Planting
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PlantingRepository {
    fun getAll(): Flow<List<Planting>>
    fun getActive(): Flow<List<Planting>>
    suspend fun getById(id: Long): Planting?
    fun getByPlotId(plotId: Long): Flow<List<Planting>>
    fun getByCropId(cropId: Long): Flow<List<Planting>>
    fun getHistoryByPlotId(plotId: Long): Flow<List<Planting>>
    suspend fun insert(planting: Planting, plotIds: List<Long>): Long
    suspend fun update(planting: Planting, plotIds: List<Long>)
    suspend fun harvest(plantingId: Long, harvestedDate: LocalDate, isFinal: Boolean = true)
    suspend fun delete(planting: Planting)
    suspend fun getPlotIdsForPlanting(plantingId: Long): List<Long>
}
