package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.CropDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.repository.CropRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CropRepositoryImpl @Inject constructor(
    private val cropDao: CropDao
) : CropRepository {

    override fun getAll(): Flow<List<Crop>> =
        cropDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getActiveOnly(): Flow<List<Crop>> =
        cropDao.getActiveOnly().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): Crop? =
        cropDao.getById(id)?.toDomain()

    override fun getByFamilyId(familyId: Long): Flow<List<Crop>> =
        cropDao.getByFamilyId(familyId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(crop: Crop): Long =
        cropDao.insert(crop.toEntity())

    override suspend fun update(crop: Crop) =
        cropDao.update(crop.toEntity())

    override suspend fun delete(crop: Crop) =
        cropDao.delete(crop.toEntity())
}
