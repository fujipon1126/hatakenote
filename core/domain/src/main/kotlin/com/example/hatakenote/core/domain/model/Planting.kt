package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * 作付け記録
 */
data class Planting(
    val id: Long = 0,
    val cropId: Long,
    val plantedDate: LocalDate,
    val harvestedDate: LocalDate? = null,
    val note: String? = null,
    val isActive: Boolean = true,
)
