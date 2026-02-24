package com.example.hatakenote.core.domain.model

/**
 * 連作相性マスタ
 */
data class RotationIncompatibility(
    val id: Long = 0,
    val familyId: Long,
    val incompatibleFamilyId: Long,
)
