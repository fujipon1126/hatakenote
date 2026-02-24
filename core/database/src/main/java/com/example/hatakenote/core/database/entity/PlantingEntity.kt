package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.Planting
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "plantings",
    foreignKeys = [
        ForeignKey(
            entity = CropEntity::class,
            parentColumns = ["id"],
            childColumns = ["cropId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("cropId")]
)
data class PlantingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropId: Long,
    val plantedDate: LocalDate,
    val harvestedDate: LocalDate? = null,
    val note: String? = null,
    val isActive: Boolean = true,
)

fun PlantingEntity.toDomain(): Planting = Planting(
    id = id,
    cropId = cropId,
    plantedDate = plantedDate,
    harvestedDate = harvestedDate,
    note = note,
    isActive = isActive,
)

fun Planting.toEntity(): PlantingEntity = PlantingEntity(
    id = id,
    cropId = cropId,
    plantedDate = plantedDate,
    harvestedDate = harvestedDate,
    note = note,
    isActive = isActive,
)
