package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * 作付け写真
 */
data class PlantingPhoto(
    val id: Long = 0,
    val plantingId: Long,
    val filePath: String,
    val takenDate: LocalDate,
)
