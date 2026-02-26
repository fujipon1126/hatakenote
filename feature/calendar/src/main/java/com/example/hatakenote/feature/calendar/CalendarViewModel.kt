package com.example.hatakenote.feature.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * カレンダーに表示するイベント
 */
sealed class CalendarEvent {
    abstract val date: LocalDate

    data class PlantingEvent(
        override val date: LocalDate,
        val planting: Planting,
        val cropName: String,
        val cropColor: String,
    ) : CalendarEvent()

    data class ReminderEvent(
        override val date: LocalDate,
        val reminder: Reminder,
    ) : CalendarEvent()

    data class WorkLogEvent(
        override val date: LocalDate,
        val workLog: WorkLog,
        val cropName: String?,
    ) : CalendarEvent()

    data class HarvestEvent(
        override val date: LocalDate,
        val planting: Planting,
        val cropName: String,
        val cropColor: String,
    ) : CalendarEvent()
}

data class CalendarUiState(
    val isLoading: Boolean = true,
    val currentYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year,
    val currentMonth: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).monthNumber,
    val selectedDate: LocalDate? = null,
    val eventsByDate: Map<LocalDate, List<CalendarEvent>> = emptyMap(),
    val selectedDateEvents: List<CalendarEvent> = emptyList(),
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val plantingRepository: PlantingRepository,
    private val reminderRepository: ReminderRepository,
    private val workLogRepository: WorkLogRepository,
    private val cropRepository: CropRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCalendarData()
    }

    private fun loadCalendarData() {
        viewModelScope.launch {
            combine(
                plantingRepository.getAll(),
                reminderRepository.getAll(),
                workLogRepository.getAll(),
            ) { plantings, reminders, workLogs ->
                Triple(plantings, reminders, workLogs)
            }.collect { (plantings, reminders, workLogs) ->
                val events = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()

                // 作付けイベント（植え付け日）
                for (planting in plantings) {
                    val crop = cropRepository.getById(planting.cropId)
                    if (crop != null) {
                        val event = CalendarEvent.PlantingEvent(
                            date = planting.plantedDate,
                            planting = planting,
                            cropName = crop.name,
                            cropColor = crop.colorHex,
                        )
                        events.getOrPut(planting.plantedDate) { mutableListOf() }.add(event)

                        // 収穫イベント
                        planting.harvestedDate?.let { harvestDate ->
                            val harvestEvent = CalendarEvent.HarvestEvent(
                                date = harvestDate,
                                planting = planting,
                                cropName = crop.name,
                                cropColor = crop.colorHex,
                            )
                            events.getOrPut(harvestDate) { mutableListOf() }.add(harvestEvent)
                        }
                    }
                }

                // リマインダーイベント
                for (reminder in reminders) {
                    val event = CalendarEvent.ReminderEvent(
                        date = reminder.scheduledDate,
                        reminder = reminder,
                    )
                    events.getOrPut(reminder.scheduledDate) { mutableListOf() }.add(event)
                }

                // 作業記録イベント
                for (workLog in workLogs) {
                    val cropName = workLog.plantingId?.let { plantingId ->
                        val planting = plantingRepository.getById(plantingId)
                        planting?.let { cropRepository.getById(it.cropId)?.name }
                    }
                    val event = CalendarEvent.WorkLogEvent(
                        date = workLog.workDate,
                        workLog = workLog,
                        cropName = cropName,
                    )
                    events.getOrPut(workLog.workDate) { mutableListOf() }.add(event)
                }

                val selectedDate = _uiState.value.selectedDate
                val selectedDateEvents = selectedDate?.let { events[it] } ?: emptyList()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    eventsByDate = events,
                    selectedDateEvents = selectedDateEvents,
                )
            }
        }
    }

    fun selectDate(date: LocalDate) {
        val events = _uiState.value.eventsByDate[date] ?: emptyList()
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            selectedDateEvents = events,
        )
    }

    fun clearSelectedDate() {
        _uiState.value = _uiState.value.copy(
            selectedDate = null,
            selectedDateEvents = emptyList(),
        )
    }

    fun goToPreviousMonth() {
        val currentState = _uiState.value
        val newDate = LocalDate(currentState.currentYear, currentState.currentMonth, 1)
            .minus(1, DateTimeUnit.MONTH)
        _uiState.value = currentState.copy(
            currentYear = newDate.year,
            currentMonth = newDate.monthNumber,
        )
    }

    fun goToNextMonth() {
        val currentState = _uiState.value
        val newDate = LocalDate(currentState.currentYear, currentState.currentMonth, 1)
            .plus(1, DateTimeUnit.MONTH)
        _uiState.value = currentState.copy(
            currentYear = newDate.year,
            currentMonth = newDate.monthNumber,
        )
    }

    fun goToToday() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _uiState.value = _uiState.value.copy(
            currentYear = today.year,
            currentMonth = today.monthNumber,
        )
    }
}
