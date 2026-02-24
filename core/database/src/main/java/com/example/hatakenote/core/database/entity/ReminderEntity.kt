package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.Reminder
import kotlinx.datetime.LocalDate

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = PlantingEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("plantingId"),
        Index("scheduledDate")
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantingId: Long,
    val scheduledDate: LocalDate,
    val notifyDaysBefore: Int,
    val title: String,
    val message: String,
    val isCompleted: Boolean = false,
)

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    plantingId = plantingId,
    scheduledDate = scheduledDate,
    notifyDaysBefore = notifyDaysBefore,
    title = title,
    message = message,
    isCompleted = isCompleted,
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    plantingId = plantingId,
    scheduledDate = scheduledDate,
    notifyDaysBefore = notifyDaysBefore,
    title = title,
    message = message,
    isCompleted = isCompleted,
)
