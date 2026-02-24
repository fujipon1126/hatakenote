package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.PlantingPhoto
import kotlinx.coroutines.flow.Flow

interface PlantingPhotoRepository {
    fun getByPlantingId(plantingId: Long): Flow<List<PlantingPhoto>>
    suspend fun getById(id: Long): PlantingPhoto?
    suspend fun insert(photo: PlantingPhoto): Long
    suspend fun delete(photo: PlantingPhoto)
    suspend fun deleteByPlantingId(plantingId: Long)
}
