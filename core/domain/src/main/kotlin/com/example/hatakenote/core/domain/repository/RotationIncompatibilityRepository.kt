package com.example.hatakenote.core.domain.repository

import com.example.hatakenote.core.domain.model.RotationIncompatibility
import kotlinx.coroutines.flow.Flow

interface RotationIncompatibilityRepository {
    fun getAll(): Flow<List<RotationIncompatibility>>
    suspend fun getByFamilyId(familyId: Long): List<RotationIncompatibility>
    suspend fun isIncompatible(familyId: Long, targetFamilyId: Long): Boolean
    suspend fun insert(incompatibility: RotationIncompatibility): Long
    suspend fun delete(incompatibility: RotationIncompatibility)
}
