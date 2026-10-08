package com.example.hatakenote.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 写真
 * - plantingIdのみ → 栽培記録の写真（既存の使い方）
 * - plotIdのみ → 区画の観察写真（カレンダーからの使い方）
 * - 両方あり → 栽培記録の写真で区画情報も持つ
 */
data class PlantingPhoto(
    val id: Long = 0,
    val plantingId: Long? = null,
    val plotId: Long? = null,
    val filePath: String,
    val takenDate: LocalDate,
    val comment: String? = null,
    val updatedAt: Instant = Instant.fromEpochMilliseconds(0),
    val updatedBy: String? = null,
) {
    /** 特定の作付けに紐づかない区画全体の写真か */
    val isPlotOnly: Boolean
        get() = plantingId == null && plotId != null
}

/**
 * この写真を [planting] の写真として表示してよいか。
 * - plantingId が一致する写真は常に対象
 * - 区画のみの写真は、撮影日が作付け期間（植え付け日〜最終収穫日）内のものだけ対象
 * - 別の作付けに紐づく写真は対象外
 *
 * 継続収穫中も harvestedDate は最新の収穫日で更新されるため、
 * 収穫日を期間の終わりとして扱うのは栽培終了（isActive = false）の作付けのみ。
 */
fun PlantingPhoto.belongsTo(planting: Planting): Boolean {
    if (plantingId != null) return plantingId == planting.id
    if (takenDate < planting.plantedDate) return false
    val endDate = planting.harvestedDate.takeIf { !planting.isActive } ?: return true
    return takenDate <= endDate
}
