package com.example.hatakenote.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.hatakenote.core.database.entity.PlantingEntity
import com.example.hatakenote.core.database.entity.PlantingPlotCrossRef
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface PlantingDao {
    @Query("SELECT * FROM plantings ORDER BY plantedDate DESC")
    fun getAll(): Flow<List<PlantingEntity>>

    @Query("SELECT * FROM plantings WHERE isActive = 1 ORDER BY plantedDate DESC")
    fun getActive(): Flow<List<PlantingEntity>>

    @Query("SELECT * FROM plantings WHERE id = :id")
    suspend fun getById(id: Long): PlantingEntity?

    @Query("""
        SELECT p.* FROM plantings p
        INNER JOIN planting_plot_cross_ref ppc ON p.id = ppc.plantingId
        WHERE ppc.plotId = :plotId AND p.isActive = 1
        ORDER BY p.plantedDate DESC
    """)
    fun getByPlotId(plotId: Long): Flow<List<PlantingEntity>>

    @Query("""
        SELECT p.* FROM plantings p
        INNER JOIN planting_plot_cross_ref ppc ON p.id = ppc.plantingId
        WHERE ppc.plotId = :plotId
        ORDER BY p.plantedDate DESC
    """)
    fun getHistoryByPlotId(plotId: Long): Flow<List<PlantingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planting: PlantingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlantingPlotCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<PlantingPlotCrossRef>)

    @Update
    suspend fun update(planting: PlantingEntity)

    @Query("UPDATE plantings SET harvestedDate = :harvestedDate, isActive = 0 WHERE id = :plantingId")
    suspend fun harvest(plantingId: Long, harvestedDate: LocalDate)

    @Delete
    suspend fun delete(planting: PlantingEntity)

    @Query("DELETE FROM planting_plot_cross_ref WHERE plantingId = :plantingId")
    suspend fun deleteCrossRefsByPlantingId(plantingId: Long)

    @Query("SELECT plotId FROM planting_plot_cross_ref WHERE plantingId = :plantingId")
    suspend fun getPlotIdsForPlanting(plantingId: Long): List<Long>

    @Transaction
    suspend fun insertWithPlots(planting: PlantingEntity, plotIds: List<Long>): Long {
        val plantingId = insert(planting)
        val crossRefs = plotIds.map { plotId ->
            PlantingPlotCrossRef(plantingId = plantingId, plotId = plotId)
        }
        insertCrossRefs(crossRefs)
        return plantingId
    }
}
