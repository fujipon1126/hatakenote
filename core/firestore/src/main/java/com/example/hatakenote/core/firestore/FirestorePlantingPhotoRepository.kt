package com.example.hatakenote.core.firestore

import android.net.Uri
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestorePlantingPhotoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val farmRepository: FarmRepository,
) : PlantingPhotoRepository {

    private fun photosCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("planting_photos")

    private fun storageDir(farmId: String): StorageReference =
        storage.reference.child("farms").child(farmId).child("planting_photos")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<PlantingPhoto>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                photosCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlantingPhoto()
                        }
                    }
            }
        }
    }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlotId(plotId: Long): Flow<List<PlantingPhoto>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                photosCollection(farmId)
                    .whereEqualTo("plotId", plotId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toPlantingPhoto()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByDate(date: LocalDate): Flow<List<PlantingPhoto>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                photosCollection(farmId)
                    .whereEqualTo("takenDate", date.toString())
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

        // ローカルパスが渡されていれば Firebase Storage にアップロードして
        // 取得した HTTPS のダウンロードURLを Firestore に保存する。
        // 既にリモートURL(http/https)なら再アップロードしない。
        val remoteUrl = if (photo.filePath.isRemoteUrl()) {
            photo.filePath
        } else {
            uploadToStorage(farmId, newId, photo.filePath)
        }

        val photoData = hashMapOf(
            "id" to newId,
            "plantingId" to photo.plantingId,
            "plotId" to photo.plotId,
            "filePath" to remoteUrl,
            "storagePath" to storageObjectPath(farmId, newId),
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

        val doc = snapshot.documents.firstOrNull() ?: return
        deleteStorageObject(doc)
        doc.reference.delete().await()
    }

    override suspend fun deleteByPlantingId(plantingId: Long) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = photosCollection(farmId)
            .whereEqualTo("plantingId", plantingId)
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            deleteStorageObject(doc)
            doc.reference.delete().await()
        }
    }

    private fun storageObjectPath(farmId: String, photoId: Long): String =
        "farms/$farmId/planting_photos/$photoId.jpg"

    private suspend fun uploadToStorage(
        farmId: String,
        photoId: Long,
        localPath: String,
    ): String {
        val ref = storageDir(farmId).child("$photoId.jpg")
        val uri = Uri.fromFile(File(localPath))
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private suspend fun deleteStorageObject(doc: DocumentSnapshot) {
        val data = doc.data ?: return
        val storagePath = data["storagePath"] as? String
        // 新形式: storagePath を直接使う
        if (!storagePath.isNullOrBlank()) {
            runCatching { storage.reference.child(storagePath).delete().await() }
            return
        }
        // 旧形式: filePath が https の Storage URL なら getReferenceFromUrl で削除
        val filePath = data["filePath"] as? String
        if (!filePath.isNullOrBlank() && filePath.isRemoteUrl()) {
            runCatching { storage.getReferenceFromUrl(filePath).delete().await() }
        }
        // ローカルパスのみ保持していたレガシーデータは Storage にファイルがないので何もしない
    }

    private fun String.isRemoteUrl(): Boolean =
        startsWith("http://") || startsWith("https://") || startsWith("gs://")

    private fun DocumentSnapshot.toPlantingPhoto(): PlantingPhoto? {
        return try {
            val data = this.data ?: return null
            PlantingPhoto(
                id = (data["id"] as? Long) ?: return null,
                plantingId = data["plantingId"] as? Long,
                plotId = data["plotId"] as? Long,
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
