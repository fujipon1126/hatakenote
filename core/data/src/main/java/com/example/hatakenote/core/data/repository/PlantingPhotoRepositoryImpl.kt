package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.PlantingPhotoDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlantingPhotoRepositoryImpl @Inject constructor(
    private val plantingPhotoDao: PlantingPhotoDao
) : PlantingPhotoRepository {

    override fun getByPlantingId(plantingId: Long): Flow<List<PlantingPhoto>> =
        plantingPhotoDao.getByPlantingId(plantingId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): PlantingPhoto? =
        plantingPhotoDao.getById(id)?.toDomain()

    override suspend fun insert(photo: PlantingPhoto): Long =
        plantingPhotoDao.insert(photo.toEntity())

    override suspend fun delete(photo: PlantingPhoto) =
        plantingPhotoDao.delete(photo.toEntity())

    override suspend fun deleteByPlantingId(plantingId: Long) =
        plantingPhotoDao.deleteByPlantingId(plantingId)
}
