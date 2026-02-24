package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.PlantingPhoto
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "planting_photos",
    foreignKeys = [
        ForeignKey(
            entity = PlantingEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("plantingId")]
)
data class PlantingPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantingId: Long,
    val filePath: String,
    val takenDate: LocalDate,
)

fun PlantingPhotoEntity.toDomain(): PlantingPhoto = PlantingPhoto(
    id = id,
    plantingId = plantingId,
    filePath = filePath,
    takenDate = takenDate,
)

fun PlantingPhoto.toEntity(): PlantingPhotoEntity = PlantingPhotoEntity(
    id = id,
    plantingId = plantingId,
    filePath = filePath,
    takenDate = takenDate,
)
