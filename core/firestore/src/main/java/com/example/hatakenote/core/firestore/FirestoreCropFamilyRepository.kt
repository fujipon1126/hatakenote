package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import com.example.hatakenote.core.domain.repository.FarmRepository
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
class FirestoreCropFamilyRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : CropFamilyRepository {

    private fun cropFamiliesCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("cropFamilies")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<CropFamily>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                cropFamiliesCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toCropFamily()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): CropFamily? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return cropFamiliesCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toCropFamily()
    }

    override suspend fun insert(family: CropFamily): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val familyData = hashMapOf(
            "id" to newId,
            "name" to family.name,
            "rotationYears" to family.rotationYears,
        )

        cropFamiliesCollection(farmId).add(familyData).await()
        return newId
    }

    override suspend fun update(family: CropFamily) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = cropFamiliesCollection(farmId)
            .whereEqualTo("id", family.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("CropFamily not found")

        docRef.update(
            mapOf(
                "name" to family.name,
                "rotationYears" to family.rotationYears,
            )
        ).await()
    }

    override suspend fun delete(family: CropFamily) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = cropFamiliesCollection(farmId)
            .whereEqualTo("id", family.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCropFamily(): CropFamily? {
        return try {
            val data = this.data ?: return null
            CropFamily(
                id = (data["id"] as? Long) ?: return null,
                name = data["name"] as? String ?: "",
                rotationYears = (data["rotationYears"] as? Long)?.toInt() ?: 0,
            )
        } catch (e: Exception) {
            null
        }
    }
}
