package com.example.hatakenote.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.model.Weather
import com.example.hatakenote.core.domain.repository.AppSettingsRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
import com.example.hatakenote.core.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val plots: List<PlotWithCurrentPlanting> = emptyList(),
    val gridColumns: Int = 4,
    val gridRows: Int = 3,
    val upcomingReminders: List<Reminder> = emptyList(),
    val weather: Weather? = null,
    val weatherLocationName: String = "",
    val weatherError: Boolean = false,
    val isLoading: Boolean = true,
    val showAddPlotDialog: Boolean = false,
    val editingPlot: Plot? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plotRepository: PlotRepository,
    private val reminderRepository: ReminderRepository,
    private val weatherRepository: WeatherRepository,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadWeatherWithSettings()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 7日以内のリマインダーを取得
            combine(
                plotRepository.getAllWithCurrentPlantings(),
                reminderRepository.getUpcoming(days = 7),
            ) { plots, reminders ->
                Pair(plots, reminders)
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = Pair(emptyList(), emptyList())
                )
                .collect { (plots, reminders) ->
                    val maxPosition = plotRepository.getMaxGridPosition()
                    _uiState.value = _uiState.value.copy(
                        plots = plots,
                        gridColumns = maxOf(4, maxPosition.first + 1),
                        gridRows = maxOf(3, maxPosition.second + 1),
                        upcomingReminders = reminders,
                        isLoading = false,
                    )
                }
        }
    }

    private fun loadWeatherWithSettings() {
        viewModelScope.launch {
            appSettingsRepository.getSettings().collect { settings ->
                loadWeather(settings.latitude, settings.longitude, settings.locationName)
            }
        }
    }

    private suspend fun loadWeather(latitude: Double, longitude: Double, locationName: String) {
        weatherRepository.getWeather(latitude, longitude)
            .onSuccess { weather ->
                _uiState.value = _uiState.value.copy(
                    weather = weather,
                    weatherLocationName = locationName,
                    weatherError = false,
                )
            }
            .onFailure {
                _uiState.value = _uiState.value.copy(
                    weatherError = true,
                )
            }
    }

    fun refreshWeather() {
        loadWeatherWithSettings()
    }

    fun completeReminder(reminderId: Long) {
        viewModelScope.launch {
            reminderRepository.markCompleted(reminderId)
        }
    }

    fun showAddPlotDialog() {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = true, editingPlot = null)
    }

    fun showEditPlotDialog(plot: Plot) {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = true, editingPlot = plot)
    }

    fun dismissPlotDialog() {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = false, editingPlot = null)
    }

    fun savePlot(name: String, gridX: Int, gridY: Int, width: Int, height: Int) {
        viewModelScope.launch {
            val editingPlot = _uiState.value.editingPlot
            if (editingPlot != null) {
                plotRepository.update(
                    editingPlot.copy(
                        name = name,
                        gridX = gridX,
                        gridY = gridY,
                        width = width,
                        height = height,
                    )
                )
            } else {
                plotRepository.insert(
                    Plot(
                        name = name,
                        gridX = gridX,
                        gridY = gridY,
                        width = width,
                        height = height,
                    )
                )
            }
            dismissPlotDialog()
        }
    }

    fun deletePlot(plot: Plot) {
        viewModelScope.launch {
            plotRepository.delete(plot)
        }
    }
}
