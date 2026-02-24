package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.RotationIncompatibilityDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.RotationIncompatibility
import com.example.hatakenote.core.domain.repository.RotationIncompatibilityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RotationIncompatibilityRepositoryImpl @Inject constructor(
    private val rotationIncompatibilityDao: RotationIncompatibilityDao
) : RotationIncompatibilityRepository {

    override fun getAll(): Flow<List<RotationIncompatibility>> =
        rotationIncompatibilityDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getByFamilyId(familyId: Long): List<RotationIncompatibility> =
        rotationIncompatibilityDao.getByFamilyId(familyId).map { it.toDomain() }

    override suspend fun isIncompatible(familyId: Long, targetFamilyId: Long): Boolean =
        rotationIncompatibilityDao.isIncompatible(familyId, targetFamilyId)

    override suspend fun insert(incompatibility: RotationIncompatibility): Long =
        rotationIncompatibilityDao.insert(incompatibility.toEntity())

    override suspend fun delete(incompatibility: RotationIncompatibility) =
        rotationIncompatibilityDao.delete(incompatibility.toEntity())
}
