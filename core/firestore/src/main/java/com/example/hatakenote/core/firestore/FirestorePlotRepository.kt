package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePlotRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : PlotRepository {

    private fun plotsCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("plots")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Plot>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                plotsCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlot()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllWithCurrentPlantings(): Flow<List<PlotWithCurrentPlanting>> {
        // TODO: Implement with plantings join
        return getAll().map { plots ->
            plots.map { plot ->
                PlotWithCurrentPlanting(
                    plot = plot,
                    currentPlantings = emptyList(),
                )
            }
        }
    }

    override suspend fun getById(id: Long): Plot? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return plotsCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toPlot()
    }

    override suspend fun getByIdWithCurrentPlantings(id: Long): PlotWithCurrentPlanting? {
        val plot = getById(id) ?: return null
        // TODO: Implement with plantings join
        return PlotWithCurrentPlanting(
            plot = plot,
            currentPlantings = emptyList(),
        )
    }

    override suspend fun insert(plot: Plot): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val plotData = hashMapOf(
            "id" to newId,
            "name" to plot.name,
            "gridX" to plot.gridX,
            "gridY" to plot.gridY,
            "width" to plot.width,
            "height" to plot.height,
        )

        plotsCollection(farmId).add(plotData).await()
        return newId
    }

    override suspend fun update(plot: Plot) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = plotsCollection(farmId)
            .whereEqualTo("id", plot.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Plot not found")

        docRef.update(
            mapOf(
                "name" to plot.name,
                "gridX" to plot.gridX,
                "gridY" to plot.gridY,
                "width" to plot.width,
                "height" to plot.height,
            )
        ).await()
    }

    override suspend fun delete(plot: Plot) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = plotsCollection(farmId)
            .whereEqualTo("id", plot.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    override suspend fun getMaxGridPosition(): Pair<Int, Int> {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: return Pair(0, 0)

        val snapshot = plotsCollection(farmId).get().await()
        var maxX = 0
        var maxY = 0

        snapshot.documents.forEach { doc ->
            val plot = doc.toPlot()
            if (plot != null) {
                val endX = plot.gridX + plot.width - 1
                val endY = plot.gridY + plot.height - 1
                if (endX > maxX) maxX = endX
                if (endY > maxY) maxY = endY
            }
        }

        return Pair(maxX, maxY)
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPlot(): Plot? {
        return try {
            val data = this.data ?: return null
            Plot(
                id = (data["id"] as? Long) ?: return null,
                name = data["name"] as? String ?: "",
                gridX = (data["gridX"] as? Long)?.toInt() ?: 0,
                gridY = (data["gridY"] as? Long)?.toInt() ?: 0,
                width = (data["width"] as? Long)?.toInt() ?: 1,
                height = (data["height"] as? Long)?.toInt() ?: 1,
            )
        } catch (e: Exception) {
            null
        }
    }
}
