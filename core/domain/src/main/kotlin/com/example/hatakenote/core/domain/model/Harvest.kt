package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * 収穫記録（1回の収穫を表す）
 */
data class Harvest(
    val id: Long = 0,
    val plantingId: Long,
    val harvestedDate: LocalDate,
    val note: String? = null,
)
