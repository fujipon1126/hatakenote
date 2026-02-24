package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Plot
import kotlinx.coroutines.flow.Flow

interface PlotRepository {
    fun getAll(): Flow<List<Plot>>
    suspend fun getById(id: Long): Plot?
    suspend fun insert(plot: Plot): Long
    suspend fun update(plot: Plot)
    suspend fun delete(plot: Plot)
}
