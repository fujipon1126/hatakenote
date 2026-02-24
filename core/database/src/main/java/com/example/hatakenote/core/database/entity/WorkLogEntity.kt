package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.WorkType
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "work_logs",
    foreignKeys = [
        ForeignKey(
            entity = PlantingEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["plotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("plantingId"),
        Index("plotId"),
        Index("workDate")
    ]
)
data class WorkLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantingId: Long? = null,
    val plotId: Long? = null,
    val workType: WorkType,
    val workDate: LocalDate,
    val detail: String? = null,
)

fun WorkLogEntity.toDomain(): WorkLog = WorkLog(
    id = id,
    plantingId = plantingId,
    plotId = plotId,
    workType = workType,
    workDate = workDate,
    detail = detail,
)

fun WorkLog.toEntity(): WorkLogEntity = WorkLogEntity(
    id = id,
    plantingId = plantingId,
    plotId = plotId,
    workType = workType,
    workDate = workDate,
    detail = detail,
)
