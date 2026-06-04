package com.example.hatakenote.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.model.Weather
import com.example.hatakenote.core.domain.repository.AppSettingsRepository
import com.example.hatakenote.core.domain.repository.AuthRepository
import com.example.hatakenote.core.domain.repository.EntityLastViewedRepository
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.HarvestRepository
import com.example.hatakenote.core.domain.repository.MasterDataInitializer
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
import com.example.hatakenote.core.domain.repository.ViewedEntityType
import com.example.hatakenote.core.domain.repository.WeatherRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val farmName: String = "",
    val plots: List<PlotWithCurrentPlanting> = emptyList(),
    val maxNumber: Int = 0,
    val upcomingReminders: List<Reminder> = emptyList(),
    val newBadgePlotIds: Set<Long> = emptySet(),
    val weather: Weather? = null,
    val weatherLocationName: String = "",
    val weatherError: Boolean = false,
    val isLoading: Boolean = true,
    val showAddPlotDialog: Boolean = false,
    val editingPlot: Plot? = null,
    val plotDialogError: String? = null,
    val errorMessage: String? = null,
    val errorId: Long = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plotRepository: PlotRepository,
    private val reminderRepository: ReminderRepository,
    private val weatherRepository: WeatherRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val farmRepository: FarmRepository,
    private val masterDataInitializer: MasterDataInitializer,
    private val workLogRepository: WorkLogRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
    private val harvestRepository: HarvestRepository,
    private val plantingRepository: PlantingRepository,
    private val entityLastViewedRepository: EntityLastViewedRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private data class UpdateSources(
        val workLogs: List<com.example.hatakenote.core.domain.model.WorkLog>,
        val photos: List<com.example.hatakenote.core.domain.model.PlantingPhoto>,
        val harvests: List<com.example.hatakenote.core.domain.model.Harvest>,
        val allPlantings: List<com.example.hatakenote.core.domain.model.Planting>,
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        initializeMasterData()
        loadData()
        loadWeatherWithSettings()
        observeCurrentFarm()
    }

    private fun initializeMasterData() {
        viewModelScope.launch {
            try {
                masterDataInitializer.initializeIfNeeded()
            } catch (_: Exception) {
                // マスタデータ初期化失敗は致命的ではないので無視
            }
        }
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
            // combine の可変引数は最大5までなので、まず判定ソース群を集約する Flow を作る
            val updateSources = combine(
                workLogRepository.getAll(),
                plantingPhotoRepository.getAll(),
                harvestRepository.getAll(),
                plantingRepository.getAll(),
            ) { workLogs, photos, harvests, allPlantings ->
                UpdateSources(workLogs, photos, harvests, allPlantings)
            }
            val lastViewedSources = combine(
                entityLastViewedRepository.lastViewed(ViewedEntityType.PLANTING),
                entityLastViewedRepository.lastViewed(ViewedEntityType.WORK_LOG),
                entityLastViewedRepository.lastViewed(ViewedEntityType.HARVEST),
            ) { p, w, h -> Triple(p, w, h) }
            val currentUserIdFlow = authRepository.currentUser.map { it?.id }

            combine(
                plotRepository.getAllWithCurrentPlantings(),
                updateSources,
                lastViewedSources,
                currentUserIdFlow,
            ) { plots, updates, viewed, userId ->
                val (plantingLV, workLogLV, harvestLV) = viewed
                val newBadgeIds = computeNewBadgePlotIds(
                    plots = plots,
                    allPlantings = updates.allPlantings,
                    workLogs = updates.workLogs,
                    photos = updates.photos,
                    harvests = updates.harvests,
                    plantingLastViewed = plantingLV,
                    workLogLastViewed = workLogLV,
                    harvestLastViewed = harvestLV,
                    currentUserId = userId,
                )
                plots to newBadgeIds
            }
                .catch { _ ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .collect { (plots, newBadgeIds) ->
                    val maxNumber = plotRepository.getMaxNumber()
                    _uiState.value = _uiState.value.copy(
                        plots = plots,
                        maxNumber = maxNumber,
                        newBadgePlotIds = newBadgeIds,
                        isLoading = false,
                    )
                }
        }

        viewModelScope.launch {
            reminderRepository.getUpcoming(days = 7)
                .catch { /* ignore */ }
                .collect { reminders ->
                    _uiState.value = _uiState.value.copy(upcomingReminders = reminders)
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
                showError("リマインダーの完了に失敗しました")
            }
        }
    }

    fun showAddPlotDialog() {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = true, editingPlot = null, plotDialogError = null)
    }

    fun showEditPlotDialog(plot: Plot) {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = true, editingPlot = plot, plotDialogError = null)
    }

    fun dismissPlotDialog() {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = false, editingPlot = null, plotDialogError = null)
    }

    fun savePlot(name: String, side: PlotSide, number: Int) {
        viewModelScope.launch {
            try {
                val editingPlot = _uiState.value.editingPlot
                val duplicate = _uiState.value.plots.any {
                    it.plot.side == side && it.plot.number == number && it.plot.id != (editingPlot?.id ?: 0)
                }
                if (duplicate) {
                    _uiState.value = _uiState.value.copy(
                        plotDialogError = "${side.displayName()}${number} は登録済みです"
                    )
                    return@launch
                }

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
                showError("区画の保存に失敗しました")
            }
        }
    }

    fun deletePlot(plot: Plot) {
        viewModelScope.launch {
            try {
                plotRepository.delete(plot)
            } catch (e: Exception) {
                showError("区画の削除に失敗しました")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(
            errorMessage = message,
            errorId = _uiState.value.errorId + 1,
        )
    }
}
