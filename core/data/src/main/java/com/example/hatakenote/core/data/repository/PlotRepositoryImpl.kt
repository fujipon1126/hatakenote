package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.CropDao
import com.example.hatakenote.core.database.dao.PlantingDao
import com.example.hatakenote.core.database.dao.PlotDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlantingWithCrop
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.repository.PlotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlotRepositoryImpl @Inject constructor(
    private val plotDao: PlotDao,
    private val plantingDao: PlantingDao,
    private val cropDao: CropDao,
) : PlotRepository {

    override fun getAll(): Flow<List<Plot>> =
        plotDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getAllWithCurrentPlantings(): Flow<List<PlotWithCurrentPlanting>> =
        combine(
            plotDao.getAll(),
            plantingDao.getActive(),
            cropDao.getAll(),
        ) { plots, activePlantings, crops ->
            val cropMap = crops.associate { it.id to it.toDomain() }

            plots.map { plotEntity ->
                val plot = plotEntity.toDomain()
                val plotPlantingIds = plantingDao.getPlotIdsForPlanting(0) // dummy call to get suspending context

                // Get plantings for this plot
                val plantingsForPlot = activePlantings.filter { planting ->
                    val plotIds = plantingDao.getPlotIdsForPlanting(planting.id)
                    plotIds.contains(plot.id)
                }

                val plantingsWithCrop = plantingsForPlot.mapNotNull { plantingEntity ->
                    val crop = cropMap[plantingEntity.cropId]
                    crop?.let {
                        PlantingWithCrop(
                            planting = plantingEntity.toDomain(),
                            crop = it
                        )
                    }
                }

                PlotWithCurrentPlanting(
                    plot = plot,
                    currentPlantings = plantingsWithCrop
                )
            }
        }

    override suspend fun getById(id: Long): Plot? =
        plotDao.getById(id)?.toDomain()

    override suspend fun getByIdWithCurrentPlantings(id: Long): PlotWithCurrentPlanting? {
        val plotEntity = plotDao.getById(id) ?: return null
        val plot = plotEntity.toDomain()

        val activePlantings = plantingDao.getByPlotId(id).first()
        val crops = cropDao.getAll().first()
        val cropMap = crops.associate { it.id to it.toDomain() }

        val plantingsWithCrop = activePlantings.mapNotNull { plantingEntity ->
            val crop = cropMap[plantingEntity.cropId]
            crop?.let {
                PlantingWithCrop(
                    planting = plantingEntity.toDomain(),
                    crop = it
                )
            }
        }

        return PlotWithCurrentPlanting(
            plot = plot,
            currentPlantings = plantingsWithCrop
        )
    }

    override suspend fun insert(plot: Plot): Long =
        plotDao.insert(plot.toEntity())

    override suspend fun update(plot: Plot) =
        plotDao.update(plot.toEntity())

    override suspend fun delete(plot: Plot) =
        plotDao.delete(plot.toEntity())

    override suspend fun getMaxGridPosition(): Pair<Int, Int> =
        Pair(plotDao.getMaxGridX(), plotDao.getMaxGridY())
}
