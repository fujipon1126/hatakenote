package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Harvest
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.HarvestRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreHarvestRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : HarvestRepository {

    private fun harvestsCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("harvests")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Harvest>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                harvestsCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { it.toHarvest() }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlantingId(plantingId: Long): Flow<List<Harvest>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                harvestsCollection(farmId)
                    .whereEqualTo("plantingId", plantingId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents
                            .mapNotNull { doc -> doc.toHarvest() }
                            .sortedByDescending { it.harvestedDate }
                    }
            }
        }
    }

    override suspend fun insert(harvest: Harvest): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val data = hashMapOf(
            "id" to newId,
            "plantingId" to harvest.plantingId,
            "harvestedDate" to harvest.harvestedDate.toString(),
            "note" to harvest.note,
            "updatedAt" to Clock.System.now().toEpochMilliseconds(),
        )

        harvestsCollection(farmId).add(data).await()
        return newId
    }

    override suspend fun delete(harvest: Harvest) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = harvestsCollection(farmId)
            .whereEqualTo("id", harvest.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toHarvest(): Harvest? {
        return try {
            val data = this.data ?: return null
            Harvest(
                id = (data["id"] as? Long) ?: return null,
                plantingId = (data["plantingId"] as? Long) ?: return null,
                harvestedDate = (data["harvestedDate"] as? String)?.let { LocalDate.parse(it) }
                    ?: return null,
                note = data["note"] as? String,
                updatedAt = (data["updatedAt"] as? Long)?.let { Instant.fromEpochMilliseconds(it) }
                    ?: Instant.fromEpochMilliseconds(0),
            )
        } catch (e: Exception) {
            null
        }
    }
}
