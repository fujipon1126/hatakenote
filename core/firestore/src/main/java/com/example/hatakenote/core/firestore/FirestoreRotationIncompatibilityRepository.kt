package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.RotationIncompatibility
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.RotationIncompatibilityRepository
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
class FirestoreRotationIncompatibilityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : RotationIncompatibilityRepository {

    private fun rotationIncompatibilitiesCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("rotationIncompatibilities")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<RotationIncompatibility>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                rotationIncompatibilitiesCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toRotationIncompatibility()
                        }
                    }
            }
        }
    }

    override suspend fun getByFamilyId(familyId: Long): List<RotationIncompatibility> {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return emptyList()
        return rotationIncompatibilitiesCollection(farmId)
            .whereEqualTo("familyId", familyId)
            .get()
            .await()
            .documents
            .mapNotNull { it.toRotationIncompatibility() }
    }

    override suspend fun isIncompatible(familyId: Long, targetFamilyId: Long): Boolean {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return false

        // Check if there's an incompatibility record in either direction
        val snapshot1 = rotationIncompatibilitiesCollection(farmId)
            .whereEqualTo("familyId", familyId)
            .whereEqualTo("incompatibleFamilyId", targetFamilyId)
            .get()
            .await()

        if (snapshot1.documents.isNotEmpty()) return true

        val snapshot2 = rotationIncompatibilitiesCollection(farmId)
            .whereEqualTo("familyId", targetFamilyId)
            .whereEqualTo("incompatibleFamilyId", familyId)
            .get()
            .await()

        return snapshot2.documents.isNotEmpty()
    }

    override suspend fun insert(incompatibility: RotationIncompatibility): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val incompatibilityData = hashMapOf(
            "id" to newId,
            "familyId" to incompatibility.familyId,
            "incompatibleFamilyId" to incompatibility.incompatibleFamilyId,
        )

        rotationIncompatibilitiesCollection(farmId).add(incompatibilityData).await()
        return newId
    }

    override suspend fun delete(incompatibility: RotationIncompatibility) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = rotationIncompatibilitiesCollection(farmId)
            .whereEqualTo("id", incompatibility.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toRotationIncompatibility(): RotationIncompatibility? {
        return try {
            val data = this.data ?: return null
            RotationIncompatibility(
                id = (data["id"] as? Long) ?: return null,
                familyId = (data["familyId"] as? Long) ?: return null,
                incompatibleFamilyId = (data["incompatibleFamilyId"] as? Long) ?: return null,
            )
        } catch (e: Exception) {
            null
        }
    }
}
