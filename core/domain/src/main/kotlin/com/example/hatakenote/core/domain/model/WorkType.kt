package com.example.hatakenote.core.domain.model

/**
 * 作業種別
 * - 区画に紐づく作業: TILL, BASE_FERTILIZE
 * - 作付けに紐づく作業: FERTILIZE, OTHER
 */
enum class WorkType {
    /** 追肥（作付けに紐づく） */
    FERTILIZE,
    /** 耕起（区画に紐づく） */
    TILL,
    /** 元肥（区画に紐づく） */
    BASE_FERTILIZE,
    /** その他（作付けに紐づく） */
    OTHER;

    /** 区画に紐づく作業かどうか */
    fun bindToPlot(): Boolean = this == TILL || this == BASE_FERTILIZE

    /** 作付けに紐づく作業かどうか */
    fun bindToPlanting(): Boolean = this == FERTILIZE || this == OTHER
}
