package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.domain.model.Farm
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FirestoreFarmRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : FarmRepository {

    private val currentFarmIdFlow = MutableStateFlow<String?>(null)

    private val farmsCollection = firestore.collection("farms")

    override fun getFarms(): Flow<List<Farm>> {
        val userId = auth.currentUser?.uid ?: return kotlinx.coroutines.flow.flowOf(emptyList())

        return farmsCollection
            .whereArrayContains("memberIds", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toFarm()
                }
            }
    }

    override fun getFarmById(id: String): Flow<Farm?> {
        return farmsCollection.document(id)
            .snapshots()
            .map { snapshot ->
                snapshot.toFarm()
            }
    }

    override fun getCurrentFarmId(): Flow<String?> = currentFarmIdFlow

    override suspend fun setCurrentFarmId(farmId: String) {
        currentFarmIdFlow.value = farmId
    }

    override suspend fun createFarm(name: String): Result<Farm> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not signed in"))

            val farmData = hashMapOf(
                "name" to name,
                "ownerId" to userId,
                "memberIds" to listOf(userId),
                "inviteCode" to generateRandomCode(),
                "createdAt" to FieldValue.serverTimestamp(),
            )

            val docRef = farmsCollection.add(farmData).await()
            val snapshot = docRef.get().await()
            val farm = snapshot.toFarm()
                ?: return Result.failure(Exception("Failed to create farm"))

            currentFarmIdFlow.value = farm.id
            Result.success(farm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinFarm(inviteCode: String): Result<Farm> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not signed in"))

            val snapshot = farmsCollection
                .whereEqualTo("inviteCode", inviteCode)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return Result.failure(Exception("Invalid invite code"))
            }

            val farmDoc = snapshot.documents.first()
            farmDoc.reference.update("memberIds", FieldValue.arrayUnion(userId)).await()

            val updatedSnapshot = farmDoc.reference.get().await()
            val farm = updatedSnapshot.toFarm()
                ?: return Result.failure(Exception("Failed to join farm"))

            currentFarmIdFlow.value = farm.id
            Result.success(farm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveFarm(farmId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not signed in"))

            farmsCollection.document(farmId)
                .update("memberIds", FieldValue.arrayRemove(userId))
                .await()

            if (currentFarmIdFlow.value == farmId) {
                currentFarmIdFlow.value = null
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateInviteCode(farmId: String): Result<String> {
        return try {
            val newCode = generateRandomCode()
            farmsCollection.document(farmId)
                .update("inviteCode", newCode)
                .await()
            Result.success(newCode)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateRandomCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toFarm(): Farm? {
        return try {
            val data = this.data ?: return null
            Farm(
                id = this.id,
                name = data["name"] as? String ?: "",
                ownerId = data["ownerId"] as? String ?: "",
                memberIds = (data["memberIds"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                inviteCode = data["inviteCode"] as? String,
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)
                    ?.let { Instant.fromEpochSeconds(it.seconds, it.nanoseconds) }
                    ?: Clock.System.now(),
            )
        } catch (e: Exception) {
            null
        }
    }
}
