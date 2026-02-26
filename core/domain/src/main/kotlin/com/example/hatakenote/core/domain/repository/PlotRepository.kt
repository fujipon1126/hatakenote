package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import kotlinx.coroutines.flow.Flow

interface PlotRepository {
    fun getAll(): Flow<List<Plot>>
    fun getAllWithCurrentPlantings(): Flow<List<PlotWithCurrentPlanting>>
    suspend fun getById(id: Long): Plot?
    suspend fun getByIdWithCurrentPlantings(id: Long): PlotWithCurrentPlanting?
    suspend fun insert(plot: Plot): Long
    suspend fun update(plot: Plot)
    suspend fun delete(plot: Plot)
    suspend fun getMaxGridPosition(): Pair<Int, Int>
}
