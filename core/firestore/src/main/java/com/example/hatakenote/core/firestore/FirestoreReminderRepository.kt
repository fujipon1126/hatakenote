package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreReminderRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
) : ReminderRepository {

    private fun remindersCollection(farmId: String) =
        firestore.collection("farms").document(farmId).collection("reminders")

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAll(): Flow<List<Reminder>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                remindersCollection(farmId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toReminder()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getPending(): Flow<List<Reminder>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                remindersCollection(farmId)
                    .whereEqualTo("isCompleted", false)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toReminder()
                        }
                    }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getUpcoming(days: Int): Flow<List<Reminder>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val endDate = today.plus(days, DateTimeUnit.DAY)

        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                remindersCollection(farmId)
                    .whereEqualTo("isCompleted", false)
                    .whereGreaterThanOrEqualTo("scheduledDate", today.toString())
                    .whereLessThanOrEqualTo("scheduledDate", endDate.toString())
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toReminder()
                        }
                    }
            }
        }
    }

    override suspend fun getById(id: Long): Reminder? {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return null
        return remindersCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?.toReminder()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getByPlantingId(plantingId: Long): Flow<List<Reminder>> {
        return farmRepository.getCurrentFarmId().flatMapLatest { farmId ->
            if (farmId == null) {
                flowOf(emptyList())
            } else {
                remindersCollection(farmId)
                    .whereEqualTo("plantingId", plantingId)
                    .snapshots()
                    .map { snapshot ->
                        snapshot.documents.mapNotNull { doc ->
                            doc.toReminder()
                        }
                    }
            }
        }
    }

    override suspend fun insert(reminder: Reminder): Long {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val newId = System.currentTimeMillis()
        val reminderData = hashMapOf(
            "id" to newId,
            "plantingId" to reminder.plantingId,
            "scheduledDate" to reminder.scheduledDate.toString(),
            "notifyDaysBefore" to reminder.notifyDaysBefore,
            "title" to reminder.title,
            "message" to reminder.message,
            "isCompleted" to reminder.isCompleted,
        )

        remindersCollection(farmId).add(reminderData).await()
        return newId
    }

    override suspend fun update(reminder: Reminder) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = remindersCollection(farmId)
            .whereEqualTo("id", reminder.id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Reminder not found")

        docRef.update(
            mapOf(
                "plantingId" to reminder.plantingId,
                "scheduledDate" to reminder.scheduledDate.toString(),
                "notifyDaysBefore" to reminder.notifyDaysBefore,
                "title" to reminder.title,
                "message" to reminder.message,
                "isCompleted" to reminder.isCompleted,
            )
        ).await()
    }

    override suspend fun markCompleted(id: Long) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = remindersCollection(farmId)
            .whereEqualTo("id", id)
            .get()
            .await()

        val docRef = snapshot.documents.firstOrNull()?.reference
            ?: throw IllegalStateException("Reminder not found")

        docRef.update("isCompleted", true).await()
    }

    override suspend fun delete(reminder: Reminder) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = remindersCollection(farmId)
            .whereEqualTo("id", reminder.id)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.reference?.delete()?.await()
    }

    override suspend fun deleteByPlantingId(plantingId: Long) {
        val farmId = farmRepository.getCurrentFarmId().first()
            ?: throw IllegalStateException("No farm selected")

        val snapshot = remindersCollection(farmId)
            .whereEqualTo("plantingId", plantingId)
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toReminder(): Reminder? {
        return try {
            val data = this.data ?: return null
            Reminder(
                id = (data["id"] as? Long) ?: return null,
                plantingId = (data["plantingId"] as? Long) ?: return null,
                scheduledDate = (data["scheduledDate"] as? String)?.let { LocalDate.parse(it) }
                    ?: return null,
                notifyDaysBefore = (data["notifyDaysBefore"] as? Long)?.toInt() ?: 0,
                title = data["title"] as? String ?: "",
                message = data["message"] as? String ?: "",
                isCompleted = data["isCompleted"] as? Boolean ?: false,
            )
        } catch (e: Exception) {
            null
        }
    }
}
