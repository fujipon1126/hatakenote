package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.PlantingDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.repository.PlantingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class PlantingRepositoryImpl @Inject constructor(
    private val plantingDao: PlantingDao
) : PlantingRepository {

    override fun getAll(): Flow<List<Planting>> =
        plantingDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getActive(): Flow<List<Planting>> =
        plantingDao.getActive().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): Planting? =
        plantingDao.getById(id)?.toDomain()

    override fun getByPlotId(plotId: Long): Flow<List<Planting>> =
        plantingDao.getByPlotId(plotId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getHistoryByPlotId(plotId: Long): Flow<List<Planting>> =
        plantingDao.getHistoryByPlotId(plotId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(planting: Planting, plotIds: List<Long>): Long =
        plantingDao.insertWithPlots(planting.toEntity(), plotIds)

    override suspend fun update(planting: Planting) =
        plantingDao.update(planting.toEntity())

    override suspend fun harvest(plantingId: Long, harvestedDate: LocalDate) =
        plantingDao.harvest(plantingId, harvestedDate)

    override suspend fun delete(planting: Planting) =
        plantingDao.delete(planting.toEntity())

    override suspend fun getPlotIdsForPlanting(plantingId: Long): List<Long> =
        plantingDao.getPlotIdsForPlanting(plantingId)
}
