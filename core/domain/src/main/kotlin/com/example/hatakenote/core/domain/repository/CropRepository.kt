package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Crop
import kotlinx.coroutines.flow.Flow

interface CropRepository {
    fun getAll(): Flow<List<Crop>>
    fun getActiveOnly(): Flow<List<Crop>>
    suspend fun getById(id: Long): Crop?
    fun getByFamilyId(familyId: Long): Flow<List<Crop>>
    suspend fun insert(crop: Crop): Long
    suspend fun update(crop: Crop)
    suspend fun delete(crop: Crop)
}
