package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.database.dao.WorkLogDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.database.entity.toEntity
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class WorkLogRepositoryImpl @Inject constructor(
    private val workLogDao: WorkLogDao
) : WorkLogRepository {

    override fun getAll(): Flow<List<WorkLog>> =
        workLogDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getById(id: Long): WorkLog? =
        workLogDao.getById(id)?.toDomain()

    override fun getByPlotId(plotId: Long): Flow<List<WorkLog>> =
        workLogDao.getByPlotId(plotId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getByPlantingId(plantingId: Long): Flow<List<WorkLog>> =
        workLogDao.getByPlantingId(plantingId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkLog>> =
        workLogDao.getByDateRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getByDate(date: LocalDate): Flow<List<WorkLog>> =
        workLogDao.getByDate(date).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(workLog: WorkLog): Long =
        workLogDao.insert(workLog.toEntity())

    override suspend fun update(workLog: WorkLog) =
        workLogDao.update(workLog.toEntity())

    override suspend fun delete(workLog: WorkLog) =
        workLogDao.delete(workLog.toEntity())
}
