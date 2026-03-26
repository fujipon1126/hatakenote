package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Fertilizer
import kotlinx.coroutines.flow.Flow

interface FertilizerRepository {
    fun getAll(): Flow<List<Fertilizer>>
    suspend fun getById(id: Long): Fertilizer?
    suspend fun insert(fertilizer: Fertilizer): Long
    suspend fun update(fertilizer: Fertilizer)
    suspend fun delete(fertilizer: Fertilizer)
}
