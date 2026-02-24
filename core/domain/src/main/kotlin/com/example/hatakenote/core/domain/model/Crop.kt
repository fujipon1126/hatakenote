package com.example.hatakenote.core.domain.model

/**
 * 作物マスタ
 */
data class Crop(
    val id: Long = 0,
    val name: String,
    val familyId: Long,
    val colorHex: String,
    val isActive: Boolean = true,
)
