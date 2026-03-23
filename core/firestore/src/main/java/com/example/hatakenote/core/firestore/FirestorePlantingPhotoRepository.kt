package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
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
class FirestorePlantingPhotoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : PlantingPhotoRepository {

    private fun photosCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("planting_photos")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlantingId(plantingId: Long): Flow<List<PlantingPhoto>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                photosCollection(farmId)
                    .whereEqualTo("plantingId", plantingId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlantingPhoto()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): PlantingPhoto? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return photosCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toPlantingPhoto()
    }

    override suspend fun insert(photo: PlantingPhoto): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val photoData = hashMapOf(
            "id" to newId,
            "plantingId" to photo.plantingId,
            "filePath" to photo.filePath,
            "takenDate" to photo.takenDate.toString(),
            "comment" to photo.comment,
        )

        photosCollection(farmId).add(photoData).await()
        return newId
    }

    override suspend fun update(photo: PlantingPhoto) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = photosCollection(farmId)
            .whereEqualTo("id", photo.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Photo not found")

        val updateData = mapOf(
            "takenDate" to photo.takenDate.toString(),
            "comment" to photo.comment,
        )
        docRef.update(updateData).await()
    }

    override suspend fun delete(photo: PlantingPhoto) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = photosCollection(farmId)
            .whereEqualTo("id", photo.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    override suspend fun deleteByPlantingId(plantingId: Long) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = photosCollection(farmId)
            .whereEqualTo("plantingId", plantingId)
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toPlantingPhoto(): PlantingPhoto? {
        return try {
            val data = this.data ?: return null
            PlantingPhoto(
                id = (data["id"] as? Long) ?: return null,
                plantingId = (data["plantingId"] as? Long) ?: return null,
                filePath = (data["filePath"] as? String) ?: return null,
                takenDate = (data["takenDate"] as? String)?.let { LocalDate.parse(it) }
                    ?: return null,
                comment = data["comment"] as? String,
            )
        } catch (e: Exception) {
            null
        }
    }
}
