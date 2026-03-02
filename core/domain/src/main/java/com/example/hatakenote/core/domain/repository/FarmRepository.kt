package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.Farm
import kotlinx.coroutines.flow.Flow

interface FarmRepository {
    fun getFarms(): Flow<List<Farm>>
    fun getFarmById(id: String): Flow<Farm?>
    fun getCurrentFarmId(): Flow<String?>

    suspend fun setCurrentFarmId(farmId: String)
    suspend fun createFarm(name: String): Result<Farm>
    suspend fun joinFarm(inviteCode: String): Result<Farm>
    suspend fun leaveFarm(farmId: String): Result<Unit>
    suspend fun generateInviteCode(farmId: String): Result<String>
}
