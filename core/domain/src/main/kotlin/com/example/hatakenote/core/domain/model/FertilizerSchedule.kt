package com.example.hatakenote.core.domain.model

/**
 * 追肥スケジュールマスタ
 */
data class FertilizerSchedule(
    val id: Long = 0,
    val cropId: Long,
    val daysAfterPlanting: Int,
    val fertilizerType: String,
    val amount: String,
    val note: String = "",
)
