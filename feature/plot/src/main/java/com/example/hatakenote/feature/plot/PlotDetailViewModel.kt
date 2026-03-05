package com.example.hatakenote.feature.plot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlantingWithCrop
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import com.example.hatakenote.feature.plot.navigation.PlotDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlotDetailUiState(
    val plot: Plot? = null,
    val currentPlantings: List<PlantingWithCrop> = emptyList(),
    val pastPlantings: List<PlantingWithCrop> = emptyList(),
    val workLogs: List<WorkLog> = emptyList(),
    val isLoading: Boolean = true,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val editDialogError: String? = null,
)

@HiltViewModel
class PlotDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plotRepository: PlotRepository,
    private val plantingRepository: PlantingRepository,
    private val workLogRepository: WorkLogRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PlotDetailRoute>()
    val plotId: Long = route.plotId

    private val _uiState = MutableStateFlow(PlotDetailUiState())
    val uiState: StateFlow<PlotDetailUiState> = _uiState.asStateFlow()

    init {
        loadPlotDetails()
    }

    private fun loadPlotDetails() {
        viewModelScope.launch {
            val plotWithPlantings = plotRepository.getByIdWithCurrentPlantings(plotId)
            if (plotWithPlantings != null) {
                // Get past plantings (inactive)
                val allPlantings = plantingRepository.getHistoryByPlotId(plotId).first()
                val pastPlantings = allPlantings.filter { !it.isActive }

                // Get work logs for this plot
                val workLogs = workLogRepository.getByPlotId(plotId).first()

                _uiState.value = _uiState.value.copy(
                    plot = plotWithPlantings.plot,
                    currentPlantings = plotWithPlantings.currentPlantings,
                    pastPlantings = emptyList(), // Will be populated with crops later
                    workLogs = workLogs,
                    isLoading = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun showEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = true, editDialogError = null)
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editDialogError = null)
    }

    fun showDeleteConfirmDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = true)
    }

    fun dismissDeleteConfirmDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = false)
    }

    fun updatePlot(name: String, side: PlotSide, number: Int) {
        viewModelScope.launch {
            val currentPlot = _uiState.value.plot ?: return@launch

            // 重複チェック（自分自身は除く）
            val allPlots = plotRepository.getAll().first()
            val duplicate = allPlots.any {
                it.side == side && it.number == number && it.id != currentPlot.id
            }
            if (duplicate) {
                _uiState.value = _uiState.value.copy(
                    editDialogError = "${side.displayName()}${number} は登録済みです"
                )
                return@launch
            }

            val updatedPlot = currentPlot.copy(
                name = name,
                side = side,
                number = number,
            )
            plotRepository.update(updatedPlot)
            _uiState.value = _uiState.value.copy(
                plot = updatedPlot,
                showEditDialog = false,
            )
        }
    }


    fun deletePlot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val currentPlot = _uiState.value.plot ?: return@launch
            plotRepository.delete(currentPlot)
            dismissDeleteConfirmDialog()
            onDeleted()
        }
    }
}
