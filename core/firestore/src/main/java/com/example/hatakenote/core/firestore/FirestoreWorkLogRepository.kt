package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.FertilizerEntry
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.WorkType
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
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
class FirestoreWorkLogRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : WorkLogRepository {

    private fun workLogsCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("worklogs")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<WorkLog>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                workLogsCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toWorkLog()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): WorkLog? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return workLogsCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toWorkLog()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlotId(plotId: Long): Flow<List<WorkLog>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                workLogsCollection(farmId)
                    .whereEqualTo("plotId", plotId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toWorkLog()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlantingId(plantingId: Long): Flow<List<WorkLog>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                workLogsCollection(farmId)
                    .whereEqualTo("plantingId", plantingId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toWorkLog()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkLog>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                workLogsCollection(farmId)
                    .whereGreaterThanOrEqualTo("workDate", startDate.toString())
                    .whereLessThanOrEqualTo("workDate", endDate.toString())
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toWorkLog()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByDate(date: LocalDate): Flow<List<WorkLog>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                workLogsCollection(farmId)
                    .whereEqualTo("workDate", date.toString())
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toWorkLog()
                        }
                    }
            }
        }
    }

    override suspend fun insert(workLog: WorkLog): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val fertilizersData = workLog.fertilizers.map { entry ->
            hashMapOf(
                "fertilizerId" to entry.fertilizerId,
                "amount" to entry.amount,
            )
        }
        val workLogData = hashMapOf(
            "id" to newId,
            "plantingId" to workLog.plantingId,
            "plotId" to workLog.plotId,
            "workType" to workLog.workType.name,
            "workDate" to workLog.workDate.toString(),
            "detail" to workLog.detail,
            "fertilizers" to fertilizersData,
        )

        workLogsCollection(farmId).add(workLogData).await()
        return newId
    }

    override suspend fun update(workLog: WorkLog) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = workLogsCollection(farmId)
            .whereEqualTo("id", workLog.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("WorkLog not found")

        val fertilizersData = workLog.fertilizers.map { entry ->
            hashMapOf(
                "fertilizerId" to entry.fertilizerId,
                "amount" to entry.amount,
            )
        }
        docRef.update(
            mapOf(
                "plantingId" to workLog.plantingId,
                "plotId" to workLog.plotId,
                "workType" to workLog.workType.name,
                "workDate" to workLog.workDate.toString(),
                "detail" to workLog.detail,
                "fertilizers" to fertilizersData,
            )
        ).await()
    }

    override suspend fun delete(workLog: WorkLog) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = workLogsCollection(farmId)
            .whereEqualTo("id", workLog.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    @Suppress("UNCHECKED_CAST")
    private fun com.google.firebase.firestore.DocumentSnapshot.toWorkLog(): WorkLog? {
        return try {
            val data = this.data ?: return null

            // 新フォーマット: fertilizers リスト
            val fertilizers = (data["fertilizers"] as? List<Map<String, Any?>>)?.mapNotNull { map ->
                val fId = map["fertilizerId"] as? Long ?: return@mapNotNull null
                val amount = map["amount"] as? String ?: ""
                FertilizerEntry(fertilizerId = fId, amount = amount)
            }
            // 旧フォーマット: 単一 fertilizerId / fertilizerAmount からの変換
                ?: listOfNotNull(
                    (data["fertilizerId"] as? Long)?.let { fId ->
                        FertilizerEntry(
                            fertilizerId = fId,
                            amount = (data["fertilizerAmount"] as? String) ?: "",
                        )
                    }
                )

            WorkLog(
                id = (data["id"] as? Long) ?: return null,
                plantingId = data["plantingId"] as? Long,
                plotId = data["plotId"] as? Long,
                workType = (data["workType"] as? String)?.let { WorkType.valueOf(it) }
                    ?: return null,
                workDate = (data["workDate"] as? String)?.let { LocalDate.parse(it) }
                    ?: return null,
                detail = data["detail"] as? String,
                fertilizers = fertilizers,
            )
        } catch (e: Exception) {
            null
        }
    }
}
