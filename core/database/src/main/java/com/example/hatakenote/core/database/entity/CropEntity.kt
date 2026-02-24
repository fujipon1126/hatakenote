package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.Crop

@Entity(
    tableName = "crops",
    foreignKeys = [
        ForeignKey(
            entity = CropFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("familyId")]
)
data class CropEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val familyId: Long,
    val colorHex: String,
    val isActive: Boolean = true,
)

fun CropEntity.toDomain(): Crop = Crop(
    id = id,
    name = name,
    familyId = familyId,
    colorHex = colorHex,
    isActive = isActive,
)

fun Crop.toEntity(): CropEntity = CropEntity(
    id = id,
    name = name,
    familyId = familyId,
    colorHex = colorHex,
    isActive = isActive,
)
