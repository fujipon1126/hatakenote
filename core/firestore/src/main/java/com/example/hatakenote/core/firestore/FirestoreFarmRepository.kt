package com.example.hatakenote.core.firestore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hatakenote.core.domain.model.Farm
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private val Context.farmDataStore: DataStore<Preferences> by preferencesDataStore(name = "farm_settings")

@Singleton
class FirestoreFarmRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : FarmRepository {

    private object PreferencesKeys {
        val CURRENT_FARM_ID = stringPreferencesKey("current_farm_id")
    }

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

    override fun getCurrentFarmId(): Flow<String?> {
        return context.farmDataStore.data.map { preferences ->
            preferences[PreferencesKeys.CURRENT_FARM_ID]
        }
    }

    override suspend fun setCurrentFarmId(farmId: String) {
        context.farmDataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_FARM_ID] = farmId
        }
    }

    private suspend fun clearCurrentFarmId() {
        context.farmDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.CURRENT_FARM_ID)
        }
    }

    override suspend fun createFarm(name: String): Result<Farm> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not signed in"))

            val inviteCode = generateRandomCode()
            val now = Clock.System.now()

            val farmData = hashMapOf(
                "name" to name,
                "ownerId" to userId,
                "memberIds" to listOf(userId),
                "inviteCode" to inviteCode,
                "createdAt" to FieldValue.serverTimestamp(),
            )

            val docRef = farmsCollection.add(farmData).await()

            val farm = Farm(
                id = docRef.id,
                name = name,
                ownerId = userId,
                memberIds = listOf(userId),
                inviteCode = inviteCode,
                createdAt = now,
            )

            setCurrentFarmId(farm.id)
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

            setCurrentFarmId(farm.id)
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

            if (getCurrentFarmId().first() == farmId) {
                clearCurrentFarmId()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFarm(farmId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not signed in"))

            val snapshot = farmsCollection.document(farmId).get().await()
            val ownerId = snapshot.getString("ownerId")

            if (ownerId != userId) {
                return Result.failure(Exception("Only the owner can delete this farm"))
            }

            farmsCollection.document(farmId).delete().await()

            if (getCurrentFarmId().first() == farmId) {
                clearCurrentFarmId()
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
