package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.PlotDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.repository.PlotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlotRepositoryImpl @Inject constructor(
    private val plotDao: PlotDao
) : PlotRepository {

    override fun getAll(): Flow<List<Plot>> =
        plotDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): Plot? =
        plotDao.getById(id)?.toDomain()

    override suspend fun insert(plot: Plot): Long =
        plotDao.insert(plot.toEntity())

    override suspend fun update(plot: Plot) =
        plotDao.update(plot.toEntity())

    override suspend fun delete(plot: Plot) =
        plotDao.delete(plot.toEntity())
}
