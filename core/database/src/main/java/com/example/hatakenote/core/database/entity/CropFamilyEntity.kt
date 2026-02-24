package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.CropFamily

@Entity(tableName = "crop_families")
data class CropFamilyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rotationYears: Int,
)

fun CropFamilyEntity.toDomain(): CropFamily = CropFamily(
    id = id,
    name = name,
    rotationYears = rotationYears,
)

fun CropFamily.toEntity(): CropFamilyEntity = CropFamilyEntity(
    id = id,
    name = name,
    rotationYears = rotationYears,
)
