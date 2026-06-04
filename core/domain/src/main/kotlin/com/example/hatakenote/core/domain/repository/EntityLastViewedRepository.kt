package com.example.hatakenote.core.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

enum class ViewedEntityType {
    PLANTING,
    WORK_LOG,
    HARVEST,
}

/**
 * 種類別 (作付け / 作業ログ / 収穫) のエンティティ単位で、
 * 「自分が最後にそのエンティティを編集画面などで見た時刻」を保持する。
 * 区画詳細画面の各ブロックにある NEW バッジの判定に使う。
 */
interface EntityLastViewedRepository {
    fun lastViewed(type: ViewedEntityType): Flow<Map<Long, Instant>>
    suspend fun markViewed(type: ViewedEntityType, id: Long)
    suspend fun markManyViewed(type: ViewedEntityType, ids: Collection<Long>)
}
