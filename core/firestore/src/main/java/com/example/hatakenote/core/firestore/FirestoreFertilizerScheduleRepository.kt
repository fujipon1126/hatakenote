package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.FertilizerSchedule
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.FertilizerScheduleRepository
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
class FirestoreFertilizerScheduleRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : FertilizerScheduleRepository {

    private fun fertilizerSchedulesCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("fertilizerSchedules")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<FertilizerSchedule>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                fertilizerSchedulesCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toFertilizerSchedule()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): FertilizerSchedule? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return fertilizerSchedulesCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toFertilizerSchedule()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByCropId(cropId: Long): Flow<List<FertilizerSchedule>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                fertilizerSchedulesCollection(farmId)
                    .whereEqualTo("cropId", cropId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toFertilizerSchedule()
                        }
                    }
            }
        }
    }

    override suspend fun insert(schedule: FertilizerSchedule): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val scheduleData = hashMapOf(
            "id" to newId,
            "cropId" to schedule.cropId,
            "daysAfterPlanting" to schedule.daysAfterPlanting,
            "fertilizerType" to schedule.fertilizerType,
            "amount" to schedule.amount,
            "note" to schedule.note,
        )

        fertilizerSchedulesCollection(farmId).add(scheduleData).await()
        return newId
    }

    override suspend fun update(schedule: FertilizerSchedule) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = fertilizerSchedulesCollection(farmId)
            .whereEqualTo("id", schedule.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("FertilizerSchedule not found")

        docRef.update(
            mapOf(
                "cropId" to schedule.cropId,
                "daysAfterPlanting" to schedule.daysAfterPlanting,
                "fertilizerType" to schedule.fertilizerType,
                "amount" to schedule.amount,
                "note" to schedule.note,
            )
        ).await()
    }

    override suspend fun delete(schedule: FertilizerSchedule) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = fertilizerSchedulesCollection(farmId)
            .whereEqualTo("id", schedule.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFertilizerSchedule(): FertilizerSchedule? {
        return try {
            val data = this.data ?: return null
            FertilizerSchedule(
                id = (data["id"] as? Long) ?: return null,
                cropId = (data["cropId"] as? Long) ?: return null,
                daysAfterPlanting = (data["daysAfterPlanting"] as? Long)?.toInt() ?: 0,
                fertilizerType = data["fertilizerType"] as? String ?: "",
                amount = data["amount"] as? String ?: "",
                note = data["note"] as? String ?: "",
            )
        } catch (e: Exception) {
            null
        }
    }
}
