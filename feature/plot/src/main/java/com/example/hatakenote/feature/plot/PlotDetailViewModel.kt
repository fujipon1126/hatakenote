package com.example.hatakenote.feature.plot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlantingWithCrop
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.Fertilizer
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.FertilizerRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import com.example.hatakenote.core.domain.usecase.GetRotationAdviceUseCase
import com.example.hatakenote.core.domain.usecase.RotationAdvice
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
    val siblingPlots: List<Plot> = emptyList(),
    val currentPlantings: List<PlantingWithCrop> = emptyList(),
    val pastPlantings: List<PlantingWithCrop> = emptyList(),
    val workLogs: List<WorkLog> = emptyList(),
    val fertilizerMap: Map<Long, Fertilizer> = emptyMap(),
    val photosByPlantingId: Map<Long, List<PlantingPhoto>> = emptyMap(),
    val isLoading: Boolean = true,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val editDialogError: String? = null,
    val plantingToDelete: PlantingWithCrop? = null,
    val rotationAdvice: RotationAdvice? = null,
)

@HiltViewModel
class PlotDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plotRepository: PlotRepository,
    private val plantingRepository: PlantingRepository,
    private val cropRepository: CropRepository,
    private val workLogRepository: WorkLogRepository,
    private val fertilizerRepository: FertilizerRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
    private val getRotationAdviceUseCase: GetRotationAdviceUseCase,
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

                // 兄弟区画（同じ作付けを共有する区画）のIDを収集
                val plantingIds = plotWithPlantings.currentPlantings.map { it.planting.id }
                val siblingPlotIds = mutableSetOf(plotId)
                for (plantingId in plantingIds) {
                    val plotIds = plantingRepository.getPlotIdsForPlanting(plantingId)
                    siblingPlotIds.addAll(plotIds)
                }

                // 兄弟区画のPlotオブジェクトを取得（名前表示用）
                val allPlots = plotRepository.getAll().first()
                val siblingPlots = siblingPlotIds
                    .filter { it != plotId }
                    .mapNotNull { sibId -> allPlots.find { it.id == sibId } }
                    .sortedWith(compareBy({ it.side }, { it.number }))

                // 全兄弟区画の plot-bound 作業記録を取得（TILL, BASE_FERTILIZE）
                val plotWorkLogs = siblingPlotIds.flatMap { sibPlotId ->
                    workLogRepository.getByPlotId(sibPlotId).first()
                }

                // planting-bound 作業記録を取得（FERTILIZE, OTHER）
                val plantingWorkLogs = plantingIds.flatMap { plantingId ->
                    workLogRepository.getByPlantingId(plantingId).first()
                }

                val workLogs = (plotWorkLogs + plantingWorkLogs)
                    .distinctBy { it.id }
                    .sortedWith(compareByDescending<WorkLog> { it.workDate }.thenByDescending { it.id })

                val rotationAdvice = getRotationAdviceUseCase(plotId)

                // 肥料マスターをMapとして取得
                val fertilizers = fertilizerRepository.getAll().first()
                val fertilizerMap = fertilizers.associateBy { it.id }

                // 現在の作付けに紐づく写真を取得（plantingId経由 + plotId経由）
                val photosByPlantingId = mutableMapOf<Long, List<PlantingPhoto>>()
                val plotPhotos = plantingPhotoRepository.getByPlotId(plotId).first()
                for (pwc in plotWithPlantings.currentPlantings) {
                    val pid = pwc.planting.id
                    val plantingPhotos = plantingPhotoRepository.getByPlantingId(pid).first()
                    val combined = (plantingPhotos + plotPhotos)
                        .distinctBy { it.id }
                        .sortedByDescending { it.takenDate }
                    if (combined.isNotEmpty()) {
                        photosByPlantingId[pid] = combined
                    }
                }

                _uiState.value = _uiState.value.copy(
                    plot = plotWithPlantings.plot,
                    siblingPlots = siblingPlots,
                    currentPlantings = plotWithPlantings.currentPlantings,
                    pastPlantings = pastPlantings.map { planting ->
                        PlantingWithCrop(
                            planting = planting,
                            crop = cropRepository.getById(planting.cropId) ?: return@launch,
                        )
                    },
                    workLogs = workLogs,
                    fertilizerMap = fertilizerMap,
                    photosByPlantingId = photosByPlantingId,
                    rotationAdvice = rotationAdvice,
                    isLoading = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun refreshData() {
        loadPlotDetails()
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


    fun showDeletePlantingDialog(plantingWithCrop: PlantingWithCrop) {
        _uiState.value = _uiState.value.copy(plantingToDelete = plantingWithCrop)
    }

    fun dismissDeletePlantingDialog() {
        _uiState.value = _uiState.value.copy(plantingToDelete = null)
    }

    fun deletePlanting() {
        val plantingWithCrop = _uiState.value.plantingToDelete ?: return
        viewModelScope.launch {
            plantingRepository.delete(plantingWithCrop.planting)
            _uiState.value = _uiState.value.copy(
                currentPlantings = _uiState.value.currentPlantings - plantingWithCrop,
                plantingToDelete = null,
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
