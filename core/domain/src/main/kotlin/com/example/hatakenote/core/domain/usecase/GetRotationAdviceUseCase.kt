package com.example.hatakenote.core.domain.usecase

import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.RotationIncompatibilityRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject

data class RotationAdvice(
    val safeCrops: List<CropAdvice>,
    val avoidCrops: List<CropAdvice>,
)

data class CropAdvice(
    val crop: Crop,
    val familyName: String,
    val reason: String?,
)

/**
 * 区画の作付け履歴をもとに、次に植える作物のおすすめ・注意を判定するUseCase
 */
class GetRotationAdviceUseCase @Inject constructor(
    private val plantingRepository: PlantingRepository,
    private val cropRepository: CropRepository,
    private val cropFamilyRepository: CropFamilyRepository,
    private val rotationIncompatibilityRepository: RotationIncompatibilityRepository,
) {
    suspend operator fun invoke(plotId: Long): RotationAdvice {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // 区画の全作付け履歴を取得
        val plantingHistory = plantingRepository.getHistoryByPlotId(plotId).first()

        // 科マスタをマップ化
        val allFamilies = cropFamilyRepository.getAll().first()
        val familyMap = allFamilies.associateBy { it.id }

        // 履歴から科ごとの直近作付け日を算出
        // familyId -> (most recent date, crop name)
        val recentFamilyPlantings = mutableMapOf<Long, Pair<LocalDate, String>>()
        for (planting in plantingHistory) {
            val crop = cropRepository.getById(planting.cropId) ?: continue
            val refDate = planting.harvestedDate ?: planting.plantedDate
            val existing = recentFamilyPlantings[crop.familyId]
            if (existing == null || refDate > existing.first) {
                recentFamilyPlantings[crop.familyId] = refDate to crop.name
            }
        }

        // 相性の悪い科のマップを構築（双方向）
        val allIncompatibilities = rotationIncompatibilityRepository.getAll().first()
        val incompatibleMap = mutableMapOf<Long, MutableSet<Long>>()
        for (inc in allIncompatibilities) {
            incompatibleMap.getOrPut(inc.familyId) { mutableSetOf() }.add(inc.incompatibleFamilyId)
            incompatibleMap.getOrPut(inc.incompatibleFamilyId) { mutableSetOf() }.add(inc.familyId)
        }

        // 有効な作物を判定
        val activeCrops = cropRepository.getActiveOnly().first()
        val safeCrops = mutableListOf<CropAdvice>()
        val avoidCrops = mutableListOf<CropAdvice>()

        for (crop in activeCrops) {
            val family = familyMap[crop.familyId] ?: continue
            val avoidReason = checkAvoidReason(crop, family.name, today, recentFamilyPlantings, familyMap, incompatibleMap)
            if (avoidReason != null) {
                avoidCrops.add(CropAdvice(crop = crop, familyName = family.name, reason = avoidReason))
            } else {
                safeCrops.add(CropAdvice(crop = crop, familyName = family.name, reason = null))
            }
        }

        return RotationAdvice(
            safeCrops = safeCrops.sortedBy { it.crop.name },
            avoidCrops = avoidCrops.sortedBy { it.crop.name },
        )
    }

    private fun checkAvoidReason(
        crop: Crop,
        cropFamilyName: String,
        today: LocalDate,
        recentFamilyPlantings: Map<Long, Pair<LocalDate, String>>,
        familyMap: Map<Long, CropFamily>,
        incompatibleMap: Map<Long, Set<Long>>,
    ): String? {
        // 同じ科の連作チェック
        val sameFamilyPlanting = recentFamilyPlantings[crop.familyId]
        if (sameFamilyPlanting != null) {
            val family = familyMap[crop.familyId] ?: return null
            val cutoff = today.minus(family.rotationYears, DateTimeUnit.YEAR)
            if (sameFamilyPlanting.first >= cutoff) {
                val yearsAgo = formatYearsAgo(sameFamilyPlanting.first, today)
                return "${yearsAgo}${cropFamilyName}（${sameFamilyPlanting.second}）を栽培。${family.rotationYears}年以上空けてください。"
            }
        }

        // 相性の悪い科のチェック
        val incompatibleFamilyIds = incompatibleMap[crop.familyId] ?: emptySet()
        for ((plantedFamilyId, plantingInfo) in recentFamilyPlantings) {
            if (plantedFamilyId !in incompatibleFamilyIds) continue
            val plantedFamily = familyMap[plantedFamilyId] ?: continue
            val cutoff = today.minus(plantedFamily.rotationYears, DateTimeUnit.YEAR)
            if (plantingInfo.first >= cutoff) {
                val yearsAgo = formatYearsAgo(plantingInfo.first, today)
                return "${yearsAgo}${plantedFamily.name}（${plantingInfo.second}）を栽培。${plantedFamily.name}と${cropFamilyName}は相性が悪いです。"
            }
        }

        return null
    }

    private fun formatYearsAgo(pastDate: LocalDate, today: LocalDate): String {
        val monthsAgo = (today.year - pastDate.year) * 12 + (today.monthNumber - pastDate.monthNumber)
        return when {
            monthsAgo < 12 -> "今年"
            monthsAgo < 24 -> "去年"
            monthsAgo < 36 -> "一昨年"
            else -> "${monthsAgo / 12}年前に"
        }
    }
}
