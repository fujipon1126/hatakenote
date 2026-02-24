package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.FertilizerScheduleDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.FertilizerSchedule
import com.example.hatakenote.core.domain.repository.FertilizerScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FertilizerScheduleRepositoryImpl @Inject constructor(
    private val fertilizerScheduleDao: FertilizerScheduleDao
) : FertilizerScheduleRepository {

    override fun getAll(): Flow<List<FertilizerSchedule>> =
        fertilizerScheduleDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): FertilizerSchedule? =
        fertilizerScheduleDao.getById(id)?.toDomain()

    override fun getByCropId(cropId: Long): Flow<List<FertilizerSchedule>> =
        fertilizerScheduleDao.getByCropId(cropId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(schedule: FertilizerSchedule): Long =
        fertilizerScheduleDao.insert(schedule.toEntity())

    override suspend fun update(schedule: FertilizerSchedule) =
        fertilizerScheduleDao.update(schedule.toEntity())

    override suspend fun delete(schedule: FertilizerSchedule) =
        fertilizerScheduleDao.delete(schedule.toEntity())
}
