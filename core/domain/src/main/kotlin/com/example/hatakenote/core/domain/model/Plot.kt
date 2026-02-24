package com.example.hatakenote.core.domain.model

/**
 * 区画
 */
data class Plot(
    val id: Long = 0,
    val name: String,
    val gridX: Int,
    val gridY: Int,
    val width: Int = 1,
    val height: Int = 1,
)
