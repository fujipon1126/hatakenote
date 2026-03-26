package com.example.hatakenote.core.firestore

import com.example.hatakenote.core.database.dao.PlotDao
import com.example.hatakenote.core.database.entity.toDomain
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.MasterDataInitializer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreMasterDataInitializer @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val farmRepository: FarmRepository,
    private val plotDao: PlotDao,
) : MasterDataInitializer {

    override suspend fun initializeIfNeeded() {
        val farmId = farmRepository.getCurrentFarmId().first() ?: return
        initializeForFarm(farmId)
        migratePlotsFromRoom(farmId)
    }

    suspend fun initializeForFarm(farmId: String) {
        val farmDoc = firestore.collection("farms").document(farmId)

        // 科マスタが存在するかチェック
        val existingFamilies = farmDoc.collection("cropFamilies")
            .limit(1)
            .get()
            .await()

        if (!existingFamilies.isEmpty) return

        // バッチ書き込みでマスタデータを一括投入
        val batch = firestore.batch()

        // 科マスタ
        families.forEach { family ->
            val ref = farmDoc.collection("cropFamilies").document()
            batch.set(ref, family)
        }

        // 作物マスタ
        crops.forEach { crop ->
            val ref = farmDoc.collection("crops").document()
            batch.set(ref, crop)
        }

        // 連作相性
        incompatibilities.forEach { incompatibility ->
            val ref = farmDoc.collection("rotationIncompatibilities").document()
            batch.set(ref, incompatibility)
        }

        batch.commit().await()
    }

    /**
     * Roomの区画データをFirestoreに一回限り移行する。
     * Firestoreのplotsが空かつRoomにデータがある場合のみ実行。
     */
    private suspend fun migratePlotsFromRoom(farmId: String) {
        val farmDoc = firestore.collection("farms").document(farmId)

        // サーバーのみ確認（ローカルキャッシュを無視）
        val existingPlots = farmDoc.collection("plots")
            .limit(1)
            .get(Source.SERVER)
            .await()

        if (!existingPlots.isEmpty) return

        val roomPlots = plotDao.getAll().first()
        if (roomPlots.isEmpty()) return

        val batch = firestore.batch()
        roomPlots.forEach { entity ->
            val plot = entity.toDomain()
            val ref = farmDoc.collection("plots").document()
            batch.set(
                ref, hashMapOf(
                    "id" to plot.id,
                    "name" to plot.name,
                    "side" to plot.side.name,
                    "number" to plot.number,
                    "width" to plot.width,
                    "height" to plot.height,
                )
            )
        }
        batch.commit().await()
    }

    companion object {

        private val families = listOf(
            mapOf("id" to 1L, "name" to "ナス科", "rotationYears" to 4),
            mapOf("id" to 2L, "name" to "アブラナ科", "rotationYears" to 2),
            mapOf("id" to 3L, "name" to "ウリ科", "rotationYears" to 3),
            mapOf("id" to 4L, "name" to "マメ科", "rotationYears" to 4),
            mapOf("id" to 5L, "name" to "ヒガンバナ科", "rotationYears" to 2),
            mapOf("id" to 6L, "name" to "キク科", "rotationYears" to 2),
            mapOf("id" to 7L, "name" to "セリ科", "rotationYears" to 2),
            mapOf("id" to 8L, "name" to "ヒユ科", "rotationYears" to 2),
            mapOf("id" to 9L, "name" to "バラ科", "rotationYears" to 3),
            mapOf("id" to 10L, "name" to "サトイモ科", "rotationYears" to 4),
            mapOf("id" to 11L, "name" to "アオイ科", "rotationYears" to 2),
            mapOf("id" to 12L, "name" to "イネ科", "rotationYears" to 1),
            mapOf("id" to 13L, "name" to "ヒルガオ科", "rotationYears" to 4),
        )

        private val crops = listOf(
            // ナス科
            mapOf("id" to 1L, "name" to "ミニトマト", "familyId" to 1L, "colorHex" to "#E53935", "isActive" to true),
            mapOf("id" to 2L, "name" to "じゃがいも", "familyId" to 1L, "colorHex" to "#8D6E63", "isActive" to true),
            mapOf("id" to 3L, "name" to "ナス", "familyId" to 1L, "colorHex" to "#5E35B1", "isActive" to true),
            mapOf("id" to 4L, "name" to "ピーマン", "familyId" to 1L, "colorHex" to "#4CAF50", "isActive" to true),
            // アブラナ科
            mapOf("id" to 5L, "name" to "大根", "familyId" to 2L, "colorHex" to "#ECEFF1", "isActive" to true),
            mapOf("id" to 6L, "name" to "白菜", "familyId" to 2L, "colorHex" to "#C5E1A5", "isActive" to true),
            mapOf("id" to 7L, "name" to "キャベツ", "familyId" to 2L, "colorHex" to "#81C784", "isActive" to true),
            mapOf("id" to 8L, "name" to "芽キャベツ", "familyId" to 2L, "colorHex" to "#66BB6A", "isActive" to true),
            mapOf("id" to 9L, "name" to "ブロッコリー", "familyId" to 2L, "colorHex" to "#2E7D32", "isActive" to true),
            mapOf("id" to 10L, "name" to "茎ブロッコリー", "familyId" to 2L, "colorHex" to "#388E3C", "isActive" to true),
            mapOf("id" to 11L, "name" to "カブ", "familyId" to 2L, "colorHex" to "#F5F5F5", "isActive" to true),
            mapOf("id" to 12L, "name" to "小松菜", "familyId" to 2L, "colorHex" to "#7CB342", "isActive" to true),
            mapOf("id" to 13L, "name" to "水菜", "familyId" to 2L, "colorHex" to "#9CCC65", "isActive" to true),
            // ウリ科
            mapOf("id" to 14L, "name" to "小玉すいか", "familyId" to 3L, "colorHex" to "#43A047", "isActive" to true),
            mapOf("id" to 15L, "name" to "メロン", "familyId" to 3L, "colorHex" to "#A5D6A7", "isActive" to true),
            mapOf("id" to 16L, "name" to "きゅうり", "familyId" to 3L, "colorHex" to "#66BB6A", "isActive" to true),
            mapOf("id" to 17L, "name" to "かぼちゃ", "familyId" to 3L, "colorHex" to "#FF9800", "isActive" to true),
            // マメ科
            mapOf("id" to 18L, "name" to "枝豆", "familyId" to 4L, "colorHex" to "#8BC34A", "isActive" to true),
            mapOf("id" to 19L, "name" to "そら豆", "familyId" to 4L, "colorHex" to "#689F38", "isActive" to true),
            mapOf("id" to 20L, "name" to "スナップエンドウ", "familyId" to 4L, "colorHex" to "#7CB342", "isActive" to true),
            // ヒガンバナ科
            mapOf("id" to 21L, "name" to "ネギ", "familyId" to 5L, "colorHex" to "#F1F8E9", "isActive" to true),
            mapOf("id" to 22L, "name" to "ニンニク", "familyId" to 5L, "colorHex" to "#FFECB3", "isActive" to true),
            mapOf("id" to 23L, "name" to "玉ねぎ", "familyId" to 5L, "colorHex" to "#FFF8E1", "isActive" to true),
            // キク科
            mapOf("id" to 24L, "name" to "春菊", "familyId" to 6L, "colorHex" to "#AED581", "isActive" to true),
            mapOf("id" to 25L, "name" to "レタス", "familyId" to 6L, "colorHex" to "#C8E6C9", "isActive" to true),
            // セリ科
            mapOf("id" to 26L, "name" to "人参", "familyId" to 7L, "colorHex" to "#FF7043", "isActive" to true),
            // ヒユ科
            mapOf("id" to 27L, "name" to "ほうれん草", "familyId" to 8L, "colorHex" to "#558B2F", "isActive" to true),
            // バラ科
            mapOf("id" to 28L, "name" to "イチゴ", "familyId" to 9L, "colorHex" to "#E91E63", "isActive" to true),
            // サトイモ科
            mapOf("id" to 29L, "name" to "里芋", "familyId" to 10L, "colorHex" to "#795548", "isActive" to true),
            // アオイ科
            mapOf("id" to 30L, "name" to "オクラ", "familyId" to 11L, "colorHex" to "#8BC34A", "isActive" to true),
            // イネ科
            mapOf("id" to 31L, "name" to "とうもろこし", "familyId" to 12L, "colorHex" to "#FFC107", "isActive" to true),
            // ヒルガオ科
            mapOf("id" to 32L, "name" to "さつまいも", "familyId" to 13L, "colorHex" to "#AD1457", "isActive" to true),
        )

        private val incompatibilities = buildList {
            var idCounter = 1L
            // 同じ科同士はNG
            listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L).forEach { familyId ->
                add(mapOf("id" to idCounter++, "familyId" to familyId, "incompatibleFamilyId" to familyId))
            }
            // ナス科とウリ科
            add(mapOf("id" to idCounter++, "familyId" to 1L, "incompatibleFamilyId" to 3L))
            add(mapOf("id" to idCounter++, "familyId" to 3L, "incompatibleFamilyId" to 1L))
        }
    }
}
