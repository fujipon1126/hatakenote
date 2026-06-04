package com.example.hatakenote.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 写真
 * - plantingIdのみ → 栽培記録の写真（既存の使い方）
 * - plotIdのみ → 区画の観察写真（カレンダーからの使い方）
 * - 両方あり → 栽培記録の写真で区画情報も持つ
 */
data class PlantingPhoto(
    val id: Long = 0,
    val plantingId: Long? = null,
    val plotId: Long? = null,
    val filePath: String,
    val takenDate: LocalDate,
    val comment: String? = null,
    val updatedAt: Instant = Instant.fromEpochMilliseconds(0),
)
