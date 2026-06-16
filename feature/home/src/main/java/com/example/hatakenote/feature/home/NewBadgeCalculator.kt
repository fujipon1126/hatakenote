package com.example.hatakenote.feature.home

import com.example.hatakenote.core.domain.model.Harvest
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.WorkLog
import kotlinx.datetime.Instant

/**
 * 各区画について「3ブロックいずれかに他人の未読更新が残っている」場合に NEW 対象とする。
 *
 * plantingPlotMap は `allPlantings.plotIds` から構築するので、isActive=false（収穫済み）の
 * Planting に紐づく Harvest や Planting 自身の更新も plot 単位で正しく集約できる。
 *
 * 判定条件: 未読 = `updatedBy != null && updatedBy != currentUserId && updatedAt > (viewedAt ?: 0)`
 *
 * @return NEW バッジを表示すべき plotId の集合
 */
fun computeNewBadgePlotIds(
    plots: List<PlotWithCurrentPlanting>,
    allPlantings: List<Planting>,
    workLogs: List<WorkLog>,
    photos: List<PlantingPhoto>,
    harvests: List<Harvest>,
    plantingLastViewed: Map<Long, Instant>,
    workLogLastViewed: Map<Long, Instant>,
    harvestLastViewed: Map<Long, Instant>,
    currentUserId: String?,
): Set<Long> {
    if (plots.isEmpty()) return emptySet()

    // 全 Planting (active + inactive) の plotIds から planting -> plotIds マップを構築。
    // isActive=false の Planting も Firestore 上の plotIds 配列を保持しているため、
    // 収穫済み Planting に紐づく Harvest も plot に辿り着ける。
    val plantingPlotMap: Map<Long, Set<Long>> = allPlantings
        .filter { it.plotIds.isNotEmpty() }
        .associate { it.id to it.plotIds.toSet() }

    val result = mutableSetOf<Long>()

    // 現在の作物 NEW: Planting 自身 or 紐づく PlantingPhoto の他人未読更新
    for (plotWith in plots) {
        val plotId = plotWith.plot.id
        for (pwc in plotWith.currentPlantings) {
            val planting = pwc.planting
            val viewedAt = plantingLastViewed[planting.id]
            if (isUnreadOthers(planting.updatedAt, planting.updatedBy, viewedAt, currentUserId)) {
                result.add(plotId)
                continue
            }
            val photoNew = photos.any { p ->
                p.plantingId == planting.id &&
                    isUnreadOthers(p.updatedAt, p.updatedBy, viewedAt, currentUserId)
            }
            if (photoNew) result.add(plotId)
        }
    }

    // 区画作業履歴 NEW: その WorkLog の他人未読更新
    for (workLog in workLogs) {
        if (!isUnreadOthers(workLog.updatedAt, workLog.updatedBy, workLogLastViewed[workLog.id], currentUserId)) {
            continue
        }
        result.addAll(workLog.plotIds)
        workLog.plantingId?.let { plantingId ->
            plantingPlotMap[plantingId]?.let { result.addAll(it) }
        }
    }

    // 収穫履歴 NEW (1): 過去 Planting 自身の他人未読更新（isActive=false 化や harvestedDate 設定など）
    for (planting in allPlantings.filter { !it.isActive }) {
        if (isUnreadOthers(planting.updatedAt, planting.updatedBy, plantingLastViewed[planting.id], currentUserId)) {
            result.addAll(planting.plotIds)
        }
    }

    // 収穫履歴 NEW (2): Harvest の他人未読更新
    for (harvest in harvests) {
        if (isUnreadOthers(harvest.updatedAt, harvest.updatedBy, harvestLastViewed[harvest.id], currentUserId)) {
            plantingPlotMap[harvest.plantingId]?.let { result.addAll(it) }
        }
    }

    return result
}

private fun isUnreadOthers(
    updatedAt: Instant,
    updatedBy: String?,
    viewedAt: Instant?,
    currentUserId: String?,
): Boolean {
    if (updatedBy == null) return false  // 旧データは自分扱い
    if (currentUserId != null && updatedBy == currentUserId) return false  // 自分の更新は除外
    val baseline = viewedAt ?: Instant.fromEpochMilliseconds(0)
    return updatedAt > baseline
}
