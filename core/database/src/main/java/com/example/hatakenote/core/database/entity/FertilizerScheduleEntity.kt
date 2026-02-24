package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.FertilizerSchedule

@Entity(
    tableName = "fertilizer_schedules",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["id"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cropId")]
)
data class FertilizerScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropId: Long,
    val daysAfterPlanting: Int,
    val fertilizerType: String,
    val amount: String,
    val note: String = "",
)

fun FertilizerScheduleEntity.toDomain(): FertilizerSchedule = FertilizerSchedule(
    id = id,
    cropId = cropId,
    daysAfterPlanting = daysAfterPlanting,
    fertilizerType = fertilizerType,
    amount = amount,
    note = note,
)

fun FertilizerSchedule.toEntity(): FertilizerScheduleEntity = FertilizerScheduleEntity(
    id = id,
    cropId = cropId,
    daysAfterPlanting = daysAfterPlanting,
    fertilizerType = fertilizerType,
    amount = amount,
    note = note,
)
