package com.example.hatakenote.core.domain.usecase

import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.FertilizerScheduleRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import javax.inject.Inject

/**
 * 作付け登録時にFertilizerScheduleマスターデータから
 * リマインダーを自動生成するUseCase
 */
class GenerateRemindersUseCase @Inject constructor(
    private val fertilizerScheduleRepository: FertilizerScheduleRepository,
    private val reminderRepository: ReminderRepository,
    private val cropRepository: CropRepository,
) {
    /**
     * 指定された作付けに対してリマインダーを自動生成する
     * @param plantingId 作付けID
     * @param cropId 作物ID
     * @param plantedDate 植え付け日
     * @param notifyDaysBefore 何日前に通知するか（デフォルト: 3日前）
     */
    suspend operator fun invoke(
        plantingId: Long,
        cropId: Long,
        plantedDate: LocalDate,
        notifyDaysBefore: Int = 3,
    ) {
        // 作物の追肥スケジュールを取得
        val schedules = fertilizerScheduleRepository.getByCropId(cropId).first()
        val crop = cropRepository.getById(cropId)

        if (schedules.isEmpty() || crop == null) {
            return
        }

        // 各スケジュールからリマインダーを生成
        schedules.forEach { schedule ->
            val scheduledDate = plantedDate.plus(schedule.daysAfterPlanting, DateTimeUnit.DAY)

            val reminder = Reminder(
                plantingId = plantingId,
                scheduledDate = scheduledDate,
                notifyDaysBefore = notifyDaysBefore,
                title = "${crop.name}の追肥",
                message = buildReminderMessage(
                    cropName = crop.name,
                    fertilizerType = schedule.fertilizerType,
                    amount = schedule.amount,
                    note = schedule.note,
                ),
                isCompleted = false,
            )

            reminderRepository.insert(reminder)
        }
    }

    private fun buildReminderMessage(
        cropName: String,
        fertilizerType: String,
        amount: String,
        note: String,
    ): String {
        val sb = StringBuilder()
        sb.append("${cropName}に追肥をしましょう。\n")
        sb.append("肥料: $fertilizerType\n")
        if (amount.isNotBlank()) {
            sb.append("量: $amount")
        }
        if (note.isNotBlank()) {
            sb.append("\n$note")
        }
        return sb.toString().trim()
    }
}
