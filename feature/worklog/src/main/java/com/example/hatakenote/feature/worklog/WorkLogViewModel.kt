package com.example.hatakenote.feature.worklog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.WorkType
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import com.example.hatakenote.feature.worklog.navigation.WorkLogRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

data class PlantingWithCropAndPlots(
    val planting: Planting,
    val crop: Crop,
    val plots: List<Plot>,
)

data class WorkLogUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val existingWorkLog: WorkLog? = null,
    val selectedWorkType: WorkType = WorkType.FERTILIZE,
    val workDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val detail: String = "",
    // For planting-bound work types
    val availablePlantings: List<PlantingWithCropAndPlots> = emptyList(),
    val selectedPlanting: PlantingWithCropAndPlots? = null,
    // For plot-bound work types
    val availablePlots: List<Plot> = emptyList(),
    val selectedPlot: Plot? = null,
    // Dialog states
    val showDatePicker: Boolean = false,
    val showPlantingSelector: Boolean = false,
    val showPlotSelector: Boolean = false,
    // Save state
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class WorkLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workLogRepository: WorkLogRepository,
    private val plantingRepository: PlantingRepository,
    private val plotRepository: PlotRepository,
    private val cropRepository: CropRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WorkLogRoute>()
    private val workLogId: Long? = route.workLogId
    private val initialPlantingId: Long? = route.plantingId
    private val initialPlotId: Long? = route.plotId

    private val _uiState = MutableStateFlow(WorkLogUiState())
    val uiState: StateFlow<WorkLogUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                // Load available plots
                val plots = plotRepository.getAll().first()

                // Load active plantings with crop info
                val plantings = plantingRepository.getActive().first()
                val plantingsWithCropAndPlots = plantings.mapNotNull { planting ->
                    val crop = cropRepository.getById(planting.cropId)
                    val plotIds = plantingRepository.getPlotIdsForPlanting(planting.id)
                    val plantingPlots = plots.filter { it.id in plotIds }
                    if (crop != null) {
                        PlantingWithCropAndPlots(planting, crop, plantingPlots)
                    } else null
                }

                if (workLogId != null) {
                    // Edit mode
                    val workLog = workLogRepository.getById(workLogId)
                    if (workLog != null) {
                        val selectedPlanting = if (workLog.plantingId != null) {
                            plantingsWithCropAndPlots.find { it.planting.id == workLog.plantingId }
                        } else null
                        val selectedPlot = if (workLog.plotId != null) {
                            plots.find { it.id == workLog.plotId }
                        } else null

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isEditMode = true,
                            existingWorkLog = workLog,
                            selectedWorkType = workLog.workType,
                            workDate = workLog.workDate,
                            detail = workLog.detail ?: "",
                            availablePlantings = plantingsWithCropAndPlots,
                            selectedPlanting = selectedPlanting,
                            availablePlots = plots,
                            selectedPlot = selectedPlot,
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "作業記録が見つかりません",
                        )
                    }
                } else {
                    // New work log mode
                    val initialWorkType = when {
                        initialPlantingId != null -> WorkType.FERTILIZE
                        initialPlotId != null -> WorkType.TILL
                        else -> WorkType.FERTILIZE
                    }

                    val selectedPlanting = if (initialPlantingId != null) {
                        plantingsWithCropAndPlots.find { it.planting.id == initialPlantingId }
                    } else null

                    val selectedPlot = if (initialPlotId != null) {
                        plots.find { it.id == initialPlotId }
                    } else null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = false,
                        selectedWorkType = initialWorkType,
                        availablePlantings = plantingsWithCropAndPlots,
                        selectedPlanting = selectedPlanting,
                        availablePlots = plots,
                        selectedPlot = selectedPlot,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "データの読み込みに失敗しました",
                )
            }
        }
    }

    fun selectWorkType(workType: WorkType) {
        _uiState.value = _uiState.value.copy(selectedWorkType = workType)
    }

    fun setWorkDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            workDate = date,
            showDatePicker = false,
        )
    }

    fun setDetail(detail: String) {
        _uiState.value = _uiState.value.copy(detail = detail)
    }

    fun selectPlanting(planting: PlantingWithCropAndPlots) {
        _uiState.value = _uiState.value.copy(
            selectedPlanting = planting,
            showPlantingSelector = false,
        )
    }

    fun selectPlot(plot: Plot) {
        _uiState.value = _uiState.value.copy(
            selectedPlot = plot,
            showPlotSelector = false,
        )
    }

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun showPlantingSelector() {
        _uiState.value = _uiState.value.copy(showPlantingSelector = true)
    }

    fun dismissPlantingSelector() {
        _uiState.value = _uiState.value.copy(showPlantingSelector = false)
    }

    fun showPlotSelector() {
        _uiState.value = _uiState.value.copy(showPlotSelector = true)
    }

    fun dismissPlotSelector() {
        _uiState.value = _uiState.value.copy(showPlotSelector = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun save() {
        val state = _uiState.value
        val workType = state.selectedWorkType

        // Validate based on work type
        if (workType.bindToPlanting() && state.selectedPlanting == null) {
            _uiState.value = state.copy(errorMessage = "作付けを選択してください")
            return
        }

        if (workType.bindToPlot() && state.selectedPlot == null) {
            _uiState.value = state.copy(errorMessage = "区画を選択してください")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                val workLog = WorkLog(
                    id = state.existingWorkLog?.id ?: 0,
                    plantingId = if (workType.bindToPlanting()) state.selectedPlanting?.planting?.id else null,
                    plotId = if (workType.bindToPlot()) state.selectedPlot?.id else null,
                    workType = workType,
                    workDate = state.workDate,
                    detail = state.detail.ifBlank { null },
                )

                if (state.isEditMode) {
                    workLogRepository.update(workLog)
                } else {
                    workLogRepository.insert(workLog)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存に失敗しました: ${e.message}",
                )
            }
        }
    }

    fun canSave(): Boolean {
        val state = _uiState.value
        val workType = state.selectedWorkType

        return when {
            state.isSaving -> false
            workType.bindToPlanting() && state.selectedPlanting == null -> false
            workType.bindToPlot() && state.selectedPlot == null -> false
            else -> true
        }
    }

    fun getWorkTypeLabel(workType: WorkType): String {
        return when (workType) {
            WorkType.FERTILIZE -> "追肥"
            WorkType.TILL -> "耕起"
            WorkType.BASE_FERTILIZE -> "元肥"
            WorkType.OTHER -> "その他"
        }
    }

    fun getDetailPlaceholder(): String {
        return when (_uiState.value.selectedWorkType) {
            WorkType.FERTILIZE -> "肥料の種類、量など"
            WorkType.TILL -> "作業内容など"
            WorkType.BASE_FERTILIZE -> "肥料の種類、量など"
            WorkType.OTHER -> "水やり、支柱立てなど"
        }
    }
}
