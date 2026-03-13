package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePlantingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : PlantingRepository {

    private fun plantingsCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("plantings")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Planting>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                plantingsCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlanting()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActive(): Flow<List<Planting>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                plantingsCollection(farmId)
                    .whereEqualTo("isActive", true)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlanting()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): Planting? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return plantingsCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toPlanting()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlotId(plotId: Long): Flow<List<Planting>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                plantingsCollection(farmId)
                    .whereArrayContains("plotIds", plotId)
                    .whereEqualTo("isActive", true)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlanting()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByCropId(cropId: Long): Flow<List<Planting>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                plantingsCollection(farmId)
                    .whereEqualTo("cropId", cropId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlanting()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getHistoryByPlotId(plotId: Long): Flow<List<Planting>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                plantingsCollection(farmId)
                    .whereArrayContains("plotIds", plotId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlanting()
                        }
                    }
            }
        }
    }

    override suspend fun insert(planting: Planting, plotIds: List<Long>): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val plantingData = hashMapOf(
            "id" to newId,
            "cropId" to planting.cropId,
            "plantedDate" to planting.plantedDate.toString(),
            "harvestedDate" to planting.harvestedDate?.toString(),
            "note" to planting.note,
            "isActive" to planting.isActive,
            "plotIds" to plotIds,
        )

        plantingsCollection(farmId).add(plantingData).await()
        return newId
    }

    override suspend fun update(planting: Planting) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = plantingsCollection(farmId)
            .whereEqualTo("id", planting.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Planting not found")

        docRef.update(
            mapOf(
                "cropId" to planting.cropId,
                "plantedDate" to planting.plantedDate.toString(),
                "harvestedDate" to planting.harvestedDate?.toString(),
                "note" to planting.note,
                "isActive" to planting.isActive,
            )
        ).await()
    }

    override suspend fun harvest(plantingId: Long, harvestedDate: LocalDate) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = plantingsCollection(farmId)
            .whereEqualTo("id", plantingId)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Planting not found")

        docRef.update(
            mapOf(
                "harvestedDate" to harvestedDate.toString(),
                "isActive" to false,
            )
        ).await()
    }

    override suspend fun delete(planting: Planting) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = plantingsCollection(farmId)
            .whereEqualTo("id", planting.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun getPlotIdsForPlanting(plantingId: Long): List<Long> {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return emptyList()

        val snapshot = plantingsCollection(farmId)
            .whereEqualTo("id", plantingId)
            .get()
            .await()

        val data = snapshot.documents.firstOrNull()?.data ?: return emptyList()
        return (data["plotIds"] as? List<Long>) ?: emptyList()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPlanting(): Planting? {
        return try {
            val data = this.data ?: return null
            Planting(
                id = (data["id"] as? Long) ?: return null,
                cropId = (data["cropId"] as? Long) ?: return null,
                plantedDate = (data["plantedDate"] as? String)?.let { LocalDate.parse(it) }
                    ?: return null,
                harvestedDate = (data["harvestedDate"] as? String)?.let { LocalDate.parse(it) },
                note = data["note"] as? String,
                isActive = data["isActive"] as? Boolean ?: true,
            )
        } catch (e: Exception) {
            null
        }
    }
}
