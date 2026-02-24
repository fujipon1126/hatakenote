package com.example.hatakenote.core.domain.model

import kotlinx.datetime.LocalDate

/**
 * リマインダー
 */
data class Reminder(
    val id: Long = 0,
    val plantingId: Long,
    val scheduledDate: LocalDate,
    val notifyDaysBefore: Int,
    val title: String,
    val message: String,
    val isCompleted: Boolean = false,
)
