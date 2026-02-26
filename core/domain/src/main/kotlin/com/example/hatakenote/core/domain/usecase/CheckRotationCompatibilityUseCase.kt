package com.example.hatakenote.core.domain.usecase

import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.RotationIncompatibilityRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * 連作障害の警告情報
 */
data class RotationWarning(
    val plotId: Long,
    val plotName: String,
    val message: String,
    val severity: WarningSeverity,
)

enum class WarningSeverity {
    /** 同じ科の連作 */
    HIGH,
    /** 相性の悪い科 */
    MEDIUM,
}

/**
 * 連作障害をチェックするUseCase
 * 区画の過去の作付け履歴から、植えようとしている作物との相性をチェックする
 */
class CheckRotationCompatibilityUseCase @Inject constructor(
    private val plantingRepository: PlantingRepository,
    private val cropRepository: CropRepository,
    private val cropFamilyRepository: CropFamilyRepository,
    private val rotationIncompatibilityRepository: RotationIncompatibilityRepository,
    private val plotRepository: PlotRepository,
) {
    /**
     * 指定された作物を指定された区画に植える場合の連作障害警告を取得
     * @param crop 植えようとしている作物
     * @param plotIds チェックする区画IDのリスト
     * @return 警告のリスト（問題がなければ空リスト）
     */
    suspend operator fun invoke(
        crop: Crop,
        plotIds: List<Long>,
    ): List<RotationWarning> {
        val warnings = mutableListOf<RotationWarning>()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // 植えようとしている作物の科を取得
        val targetFamily = cropFamilyRepository.getById(crop.familyId) ?: return emptyList()

        // 相性の悪い科のリストを取得
        val incompatibleFamilyIds = rotationIncompatibilityRepository
            .getByFamilyId(crop.familyId)
            .map { it.incompatibleFamilyId }
            .toSet()

        // 連作を避けるべき期間の計算用
        val rotationCutoffDate = today.minus(targetFamily.rotationYears, DateTimeUnit.YEAR)

        for (plotId in plotIds) {
            val plot = plotRepository.getById(plotId) ?: continue
            val plantingHistory = plantingRepository.getHistoryByPlotId(plotId).first()

            for (planting in plantingHistory) {
                // 収穫日または植え付け日を基準に期間をチェック
                val plantingDate = planting.harvestedDate ?: planting.plantedDate

                // rotationYears以内の作付けのみチェック
                if (plantingDate < rotationCutoffDate) {
                    continue
                }

                val pastCrop = cropRepository.getById(planting.cropId) ?: continue
                val pastFamily = cropFamilyRepository.getById(pastCrop.familyId) ?: continue

                // 同じ科の連作チェック
                if (pastCrop.familyId == crop.familyId) {
                    val yearsAgo = calculateYearsAgo(plantingDate, today)
                    warnings.add(
                        RotationWarning(
                            plotId = plotId,
                            plotName = plot.name,
                            message = buildSameFamilyMessage(
                                plotName = plot.name,
                                pastCropName = pastCrop.name,
                                familyName = pastFamily.name,
                                yearsAgo = yearsAgo,
                                rotationYears = targetFamily.rotationYears,
                            ),
                            severity = WarningSeverity.HIGH,
                        )
                    )
                    break // この区画についてはこれ以上チェック不要
                }

                // 相性の悪い科のチェック
                if (pastCrop.familyId in incompatibleFamilyIds) {
                    val yearsAgo = calculateYearsAgo(plantingDate, today)
                    warnings.add(
                        RotationWarning(
                            plotId = plotId,
                            plotName = plot.name,
                            message = buildIncompatibleFamilyMessage(
                                plotName = plot.name,
                                pastCropName = pastCrop.name,
                                pastFamilyName = pastFamily.name,
                                targetFamilyName = targetFamily.name,
                                yearsAgo = yearsAgo,
                            ),
                            severity = WarningSeverity.MEDIUM,
                        )
                    )
                    break // この区画についてはこれ以上チェック不要
                }
            }
        }

        return warnings
    }

    private fun calculateYearsAgo(
        pastDate: kotlinx.datetime.LocalDate,
        today: kotlinx.datetime.LocalDate,
    ): String {
        val monthsAgo = (today.year - pastDate.year) * 12 + (today.monthNumber - pastDate.monthNumber)
        return when {
            monthsAgo < 12 -> "今年"
            monthsAgo < 24 -> "去年"
            monthsAgo < 36 -> "一昨年"
            else -> "${monthsAgo / 12}年前"
        }
    }

    private fun buildSameFamilyMessage(
        plotName: String,
        pastCropName: String,
        familyName: String,
        yearsAgo: String,
        rotationYears: Int,
    ): String {
        return "${plotName}は${yearsAgo}${familyName}（${pastCropName}）を植えています。" +
            "${familyName}は${rotationYears}年以上間隔を空けることをお勧めします。"
    }

    private fun buildIncompatibleFamilyMessage(
        plotName: String,
        pastCropName: String,
        pastFamilyName: String,
        targetFamilyName: String,
        yearsAgo: String,
    ): String {
        return "${plotName}は${yearsAgo}${pastFamilyName}（${pastCropName}）を植えています。" +
            "${pastFamilyName}と${targetFamilyName}は相性が悪いため注意が必要です。"
    }
}
