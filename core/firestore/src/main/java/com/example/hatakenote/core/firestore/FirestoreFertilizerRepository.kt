package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Fertilizer
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.FertilizerRepository
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
class FirestoreFertilizerRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : FertilizerRepository {

    private fun fertilizersCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("fertilizers")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Fertilizer>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                fertilizersCollection(farmId)
                    .orderBy("name")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toFertilizer()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): Fertilizer? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return fertilizersCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toFertilizer()
    }

    override suspend fun insert(fertilizer: Fertilizer): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = fertilizer.id.takeIf { it != 0L } ?: System.currentTimeMillis()
        val data = hashMapOf(
            "id" to newId,
            "name" to fertilizer.name,
            "defaultAmount" to fertilizer.defaultAmount,
            "note" to fertilizer.note,
        )

        fertilizersCollection(farmId).add(data).await()
        return newId
    }

    override suspend fun update(fertilizer: Fertilizer) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = fertilizersCollection(farmId)
            .whereEqualTo("id", fertilizer.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Fertilizer not found")

        docRef.update(
            mapOf(
                "name" to fertilizer.name,
                "defaultAmount" to fertilizer.defaultAmount,
                "note" to fertilizer.note,
            )
        ).await()
    }

    override suspend fun delete(fertilizer: Fertilizer) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = fertilizersCollection(farmId)
            .whereEqualTo("id", fertilizer.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFertilizer(): Fertilizer? {
        return try {
            val data = this.data ?: return null
            Fertilizer(
                id = (data["id"] as? Long) ?: return null,
                name = data["name"] as? String ?: "",
                defaultAmount = data["defaultAmount"] as? String ?: "",
                note = data["note"] as? String ?: "",
            )
        } catch (e: Exception) {
            null
        }
    }
}
