package com.example.hatakenote.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.model.Weather
import com.example.hatakenote.core.domain.repository.AppSettingsRepository
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
import com.example.hatakenote.core.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val farmName: String = "",
    val plots: List<PlotWithCurrentPlanting> = emptyList(),
    val maxNumber: Int = 0,
    val upcomingReminders: List<Reminder> = emptyList(),
    val weather: Weather? = null,
    val weatherLocationName: String = "",
    val weatherError: Boolean = false,
    val isLoading: Boolean = true,
    val showAddPlotDialog: Boolean = false,
    val editingPlot: Plot? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plotRepository: PlotRepository,
    private val reminderRepository: ReminderRepository,
    private val weatherRepository: WeatherRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val farmRepository: FarmRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        loadWeatherWithSettings()
        observeCurrentFarm()
    }

    private fun observeCurrentFarm() {
        viewModelScope.launch {
            farmRepository.getCurrentFarmId()
                .filterNotNull()
                .flatMapLatest { farmId ->
                    farmRepository.getFarmById(farmId)
                }
                .collect { farm ->
                    _uiState.value = _uiState.value.copy(
                        farmName = farm?.name ?: ""
                    )
                }
        }
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
                .catch { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .collect { (plots, reminders) ->
                    val maxNumber = plotRepository.getMaxNumber()
                    _uiState.value = _uiState.value.copy(
                        plots = plots,
                        maxNumber = maxNumber,
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
            try {
                reminderRepository.markCompleted(reminderId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "リマインダーの完了に失敗しました"
                )
            }
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

    fun savePlot(name: String, side: PlotSide, number: Int) {
        viewModelScope.launch {
            try {
                val editingPlot = _uiState.value.editingPlot
                if (editingPlot != null) {
                    plotRepository.update(
                        editingPlot.copy(
                            name = name,
                            side = side,
                            number = number,
                        )
                    )
                } else {
                    plotRepository.insert(
                        Plot(
                            name = name,
                            side = side,
                            number = number,
                        )
                    )
                }
                dismissPlotDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "区画の保存に失敗しました"
                )
            }
        }
    }

    fun deletePlot(plot: Plot) {
        viewModelScope.launch {
            try {
                plotRepository.delete(plot)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "区画の削除に失敗しました"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
