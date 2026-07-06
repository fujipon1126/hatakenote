package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreCropRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : CropRepository {

    private fun cropsCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("crops")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Crop>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                cropsCollection(farmId)
                    .orderBy("name")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toCrop()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActiveOnly(): Flow<List<Crop>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                // 一発読み（.first()）でも完全なリストを返すよう get().await() で確定取得する。
                // snapshots().first() はコールドキャッシュ時に部分的/空の初回 emission を掴む不具合がある。
                // orderBy("name") は isActive フィルタとの複合インデックスを要求するため、
                // サーバー取得で FAILED_PRECONDITION になる。並び替えはクライアント側で行いインデックス不要にする。
                flow {
                    val snapshot = cropsCollection(farmId)
                        .whereEqualTo("isActive", true)
                        .get()
                        .await()
                    emit(
                        snapshot.documents
                            .mapNotNull { doc -> doc.toCrop() }
                            .sortedBy { it.name }
                    )
                }
            }
        }
    }

    override suspend fun getById(id: Long): Crop? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return cropsCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toCrop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByFamilyId(familyId: Long): Flow<List<Crop>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                cropsCollection(farmId)
                    .whereEqualTo("familyId", familyId)
                    .orderBy("name")
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toCrop()
                        }
                    }
            }
        }
    }

    override suspend fun insert(crop: Crop): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val cropData = hashMapOf(
            "id" to newId,
            "name" to crop.name,
            "familyId" to crop.familyId,
            "colorHex" to crop.colorHex,
            "isActive" to crop.isActive,
        )

        cropsCollection(farmId).add(cropData).await()
        return newId
    }

    override suspend fun update(crop: Crop) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = cropsCollection(farmId)
            .whereEqualTo("id", crop.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Crop not found")

        docRef.update(
            mapOf(
                "name" to crop.name,
                "familyId" to crop.familyId,
                "colorHex" to crop.colorHex,
                "isActive" to crop.isActive,
            )
        ).await()
    }

    override suspend fun delete(crop: Crop) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = cropsCollection(farmId)
            .whereEqualTo("id", crop.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toCrop(): Crop? {
        return try {
            val data = this.data ?: return null
            Crop(
                id = (data["id"] as? Long) ?: return null,
                name = data["name"] as? String ?: "",
                familyId = (data["familyId"] as? Long) ?: 0,
                colorHex = data["colorHex"] as? String ?: "#4CAF50",
                isActive = data["isActive"] as? Boolean ?: true,
            )
        } catch (e: Exception) {
            null
        }
    }
}
