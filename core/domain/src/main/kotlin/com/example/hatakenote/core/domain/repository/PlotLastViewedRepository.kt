package com.example.hatakenote.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * 区画ごとの「最後にユーザーが詳細を閲覧した時刻」を保持するリポジトリ。
 * 畑マップで「未閲覧の更新がある区画」に NEW バッジを出すために使う。
 */
interface PlotLastViewedRepository {
    fun lastViewedMap(): Flow<Map<Long, Instant>>
    suspend fun markViewed(plotId: Long)
}
