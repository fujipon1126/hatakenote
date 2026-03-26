package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.PlantingPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface PlantingPhotoRepository {
    fun getAll(): Flow<List<PlantingPhoto>>
    fun getByPlantingId(plantingId: Long): Flow<List<PlantingPhoto>>
    fun getByPlotId(plotId: Long): Flow<List<PlantingPhoto>>
    fun getByDate(date: LocalDate): Flow<List<PlantingPhoto>>
    suspend fun getById(id: Long): PlantingPhoto?
    suspend fun insert(photo: PlantingPhoto): Long
    suspend fun update(photo: PlantingPhoto)
    suspend fun delete(photo: PlantingPhoto)
    suspend fun deleteByPlantingId(plantingId: Long)
}
