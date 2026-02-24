package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.CropFamilyDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CropFamilyRepositoryImpl @Inject constructor(
    private val cropFamilyDao: CropFamilyDao
) : CropFamilyRepository {

    override fun getAll(): Flow<List<CropFamily>> =
        cropFamilyDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): CropFamily? =
        cropFamilyDao.getById(id)?.toDomain()

    override suspend fun insert(family: CropFamily): Long =
        cropFamilyDao.insert(family.toEntity())

    override suspend fun update(family: CropFamily) =
        cropFamilyDao.update(family.toEntity())

    override suspend fun delete(family: CropFamily) =
        cropFamilyDao.delete(family.toEntity())
}
