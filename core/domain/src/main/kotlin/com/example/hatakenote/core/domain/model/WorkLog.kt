package com.example.hatakenote.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 作業で使用した肥料1件分
 */
data class FertilizerEntry(
    val fertilizerId: Long,
    val amount: String = "",
)

/**
 * 作業記録
 */
data class WorkLog(
    val id: Long = 0,
    val plantingId: Long? = null,
    val plotIds: List<Long> = emptyList(),
    val workType: WorkType,
    val workDate: LocalDate,
    val detail: String? = null,
    val fertilizers: List<FertilizerEntry> = emptyList(),
    val updatedAt: Instant = Instant.fromEpochMilliseconds(0),
    val updatedBy: String? = null,
)
