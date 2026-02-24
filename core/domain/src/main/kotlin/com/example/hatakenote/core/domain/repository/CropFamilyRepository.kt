package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.CropFamily
import kotlinx.coroutines.flow.Flow

interface CropFamilyRepository {
    fun getAll(): Flow<List<CropFamily>>
    suspend fun getById(id: Long): CropFamily?
    suspend fun insert(family: CropFamily): Long
    suspend fun update(family: CropFamily)
    suspend fun delete(family: CropFamily)
}
