package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.RotationIncompatibility

@Entity(
    tableName = "rotation_incompatibilities",
    foreignKeys = [
        ForeignKey(
            entity = CropFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CropFamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["incompatibleFamilyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("familyId"),
        Index("incompatibleFamilyId"),
        Index(value = ["familyId", "incompatibleFamilyId"], unique = true)
    ]
)
data class RotationIncompatibilityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val familyId: Long,
    val incompatibleFamilyId: Long,
)

fun RotationIncompatibilityEntity.toDomain(): RotationIncompatibility = RotationIncompatibility(
    id = id,
    familyId = familyId,
    incompatibleFamilyId = incompatibleFamilyId,
)

fun RotationIncompatibility.toEntity(): RotationIncompatibilityEntity = RotationIncompatibilityEntity(
    id = id,
    familyId = familyId,
    incompatibleFamilyId = incompatibleFamilyId,
)
