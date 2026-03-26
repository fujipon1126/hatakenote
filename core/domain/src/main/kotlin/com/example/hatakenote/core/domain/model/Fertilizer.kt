package com.example.hatakenote.core.domain.model

/**
 * 肥料マスタ
 */
data class Fertilizer(
    val id: Long = 0,
    val name: String,
    val defaultAmount: String = "",
    val note: String = "",
)
