package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * 作業記録
 */
data class WorkLog(
    val id: Long = 0,
    val plantingId: Long? = null,
    val plotId: Long? = null,
    val workType: WorkType,
    val workDate: LocalDate,
    val detail: String? = null,
)
