package com.example.hatakenote.feature.home

import com.example.hatakenote.core.domain.model.Harvest
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.WorkLog
import kotlinx.datetime.Instant

/**
 * 各区画について「ユーザーが最後に閲覧した時刻」以降の更新が一つでもあれば NEW 対象とする。
 *
 * 紐づくエンティティ（Planting, WorkLog, PlantingPhoto, Harvest）は plot との対応関係を
 * 解いた上で plot 単位の最大 updatedAt を集約する。アクティブでない過去 Planting からの
 * 紐付けは現状の `PlotWithCurrentPlanting` には含まれないため、現在進行中の作付けに
 * 紐づく更新のみが対象になる（実用上の通知精度として十分）。
 *
 * @return NEW バッジを表示すべき plotId の集合
 */
fun computeNewBadgePlotIds(
    plots: List<PlotWithCurrentPlanting>,
    workLogs: List<WorkLog>,
    photos: List<PlantingPhoto>,
    harvests: List<Harvest>,
    lastViewed: Map<Long, Instant>,
): Set<Long> {
    if (plots.isEmpty()) return emptySet()

    val plantingPlotMap: Map<Long, Set<Long>> = plots
        .flatMap { plotWith ->
            plotWith.currentPlantings.map { it.planting.id to plotWith.plot.id }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { it.value.toSet() }

    val plotMaxUpdate = mutableMapOf<Long, Instant>()

    fun bump(plotId: Long, instant: Instant) {
        val current = plotMaxUpdate[plotId]
        if (current == null || instant > current) {
            plotMaxUpdate[plotId] = instant
        }
    }

    for (plotWith in plots) {
        bump(plotWith.plot.id, plotWith.plot.updatedAt)
        for (pwc in plotWith.currentPlantings) {
            bump(plotWith.plot.id, pwc.planting.updatedAt)
        }
    }

    for (workLog in workLogs) {
        workLog.plotId?.let { bump(it, workLog.updatedAt) }
        workLog.plantingId?.let { plantingId ->
            plantingPlotMap[plantingId]?.forEach { plotId ->
                bump(plotId, workLog.updatedAt)
            }
        }
    }

    for (photo in photos) {
        photo.plotId?.let { bump(it, photo.updatedAt) }
        photo.plantingId?.let { plantingId ->
            plantingPlotMap[plantingId]?.forEach { plotId ->
                bump(plotId, photo.updatedAt)
            }
        }
    }

    for (harvest in harvests) {
        plantingPlotMap[harvest.plantingId]?.forEach { plotId ->
            bump(plotId, harvest.updatedAt)
        }
    }

    val epochZero = Instant.fromEpochMilliseconds(0)
    return plotMaxUpdate
        .filter { (plotId, maxAt) -> maxAt > (lastViewed[plotId] ?: epochZero) }
        .keys
}
