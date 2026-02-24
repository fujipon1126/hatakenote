package com.example.hatakenote.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hatakenote.core.database.entity.CropEntity
import com.example.hatakenote.core.database.entity.CropFamilyEntity
import com.example.hatakenote.core.database.entity.RotationIncompatibilityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InitialDataCallback(
    private val database: () -> HatakeDatabase
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            populateDatabase(database())
        }
    }

    private suspend fun populateDatabase(database: HatakeDatabase) {
        // 科マスタ
        database.cropFamilyDao().insertAll(families)

        // 作物マスタ
        database.cropDao().insertAll(crops)

        // 連作相性
        database.rotationIncompatibilityDao().insertAll(incompatibilities)
    }

    companion object {
        val families = listOf(
            CropFamilyEntity(id = 1, name = "ナス科", rotationYears = 4),
            CropFamilyEntity(id = 2, name = "アブラナ科", rotationYears = 2),
            CropFamilyEntity(id = 3, name = "ウリ科", rotationYears = 3),
            CropFamilyEntity(id = 4, name = "マメ科", rotationYears = 4),
            CropFamilyEntity(id = 5, name = "ヒガンバナ科", rotationYears = 2),
            CropFamilyEntity(id = 6, name = "キク科", rotationYears = 2),
            CropFamilyEntity(id = 7, name = "セリ科", rotationYears = 2),
            CropFamilyEntity(id = 8, name = "ヒユ科", rotationYears = 2),
            CropFamilyEntity(id = 9, name = "バラ科", rotationYears = 3),
            CropFamilyEntity(id = 10, name = "サトイモ科", rotationYears = 4),
            CropFamilyEntity(id = 11, name = "アオイ科", rotationYears = 2),
            CropFamilyEntity(id = 12, name = "イネ科", rotationYears = 1),
            CropFamilyEntity(id = 13, name = "ヒルガオ科", rotationYears = 4),
        )

        val crops = listOf(
            // ナス科
            CropEntity(id = 1, name = "ミニトマト", familyId = 1, colorHex = "#E53935"),
            CropEntity(id = 2, name = "じゃがいも", familyId = 1, colorHex = "#8D6E63"),
            CropEntity(id = 3, name = "ナス", familyId = 1, colorHex = "#5E35B1"),
            CropEntity(id = 4, name = "ピーマン", familyId = 1, colorHex = "#4CAF50"),
            // アブラナ科
            CropEntity(id = 5, name = "大根", familyId = 2, colorHex = "#ECEFF1"),
            CropEntity(id = 6, name = "白菜", familyId = 2, colorHex = "#C5E1A5"),
            CropEntity(id = 7, name = "キャベツ", familyId = 2, colorHex = "#81C784"),
            CropEntity(id = 8, name = "芽キャベツ", familyId = 2, colorHex = "#66BB6A"),
            CropEntity(id = 9, name = "ブロッコリー", familyId = 2, colorHex = "#2E7D32"),
            CropEntity(id = 10, name = "茎ブロッコリー", familyId = 2, colorHex = "#388E3C"),
            CropEntity(id = 11, name = "カブ", familyId = 2, colorHex = "#F5F5F5"),
            CropEntity(id = 12, name = "小松菜", familyId = 2, colorHex = "#7CB342"),
            CropEntity(id = 13, name = "水菜", familyId = 2, colorHex = "#9CCC65"),
            // ウリ科
            CropEntity(id = 14, name = "小玉すいか", familyId = 3, colorHex = "#43A047"),
            CropEntity(id = 15, name = "メロン", familyId = 3, colorHex = "#A5D6A7"),
            CropEntity(id = 16, name = "きゅうり", familyId = 3, colorHex = "#66BB6A"),
            CropEntity(id = 17, name = "かぼちゃ", familyId = 3, colorHex = "#FF9800"),
            // マメ科
            CropEntity(id = 18, name = "枝豆", familyId = 4, colorHex = "#8BC34A"),
            CropEntity(id = 19, name = "そら豆", familyId = 4, colorHex = "#689F38"),
            CropEntity(id = 20, name = "スナップエンドウ", familyId = 4, colorHex = "#7CB342"),
            // ヒガンバナ科
            CropEntity(id = 21, name = "ネギ", familyId = 5, colorHex = "#F1F8E9"),
            CropEntity(id = 22, name = "ニンニク", familyId = 5, colorHex = "#FFECB3"),
            CropEntity(id = 23, name = "玉ねぎ", familyId = 5, colorHex = "#FFF8E1"),
            // キク科
            CropEntity(id = 24, name = "春菊", familyId = 6, colorHex = "#AED581"),
            CropEntity(id = 25, name = "レタス", familyId = 6, colorHex = "#C8E6C9"),
            // セリ科
            CropEntity(id = 26, name = "人参", familyId = 7, colorHex = "#FF7043"),
            // ヒユ科
            CropEntity(id = 27, name = "ほうれん草", familyId = 8, colorHex = "#558B2F"),
            // バラ科
            CropEntity(id = 28, name = "イチゴ", familyId = 9, colorHex = "#E91E63"),
            // サトイモ科
            CropEntity(id = 29, name = "里芋", familyId = 10, colorHex = "#795548"),
            // アオイ科
            CropEntity(id = 30, name = "オクラ", familyId = 11, colorHex = "#8BC34A"),
            // イネ科
            CropEntity(id = 31, name = "とうもろこし", familyId = 12, colorHex = "#FFC107"),
            // ヒルガオ科
            CropEntity(id = 32, name = "さつまいも", familyId = 13, colorHex = "#AD1457"),
        )

        val incompatibilities = buildList {
            // 同じ科同士はNG
            listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L).forEach { familyId ->
                add(
                    RotationIncompatibilityEntity(
                        familyId = familyId,
                        incompatibleFamilyId = familyId
                    )
                )
            }
            // ナス科とウリ科
            add(RotationIncompatibilityEntity(familyId = 1, incompatibleFamilyId = 3))
            add(RotationIncompatibilityEntity(familyId = 3, incompatibleFamilyId = 1))
        }
    }
}
