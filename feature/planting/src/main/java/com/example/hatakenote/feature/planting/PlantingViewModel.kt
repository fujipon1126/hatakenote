package com.example.hatakenote.feature.planting

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.feature.planting.navigation.PlantingRoute
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

data class PlantingUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val crops: List<Crop> = emptyList(),
    val plots: List<Plot> = emptyList(),
    val selectedCrop: Crop? = null,
    val selectedPlotIds: Set<Long> = emptySet(),
    val plantedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val note: String = "",
    val photos: List<PlantingPhoto> = emptyList(),
    val pendingPhotoUris: List<Uri> = emptyList(),
    val existingPlanting: Planting? = null,
    val showCropSelector: Boolean = false,
    val showPlotSelector: Boolean = false,
    val showDatePicker: Boolean = false,
    val showHarvestDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val harvestSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PlantingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cropRepository: CropRepository,
    private val plotRepository: PlotRepository,
    private val plantingRepository: PlantingRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PlantingRoute>()
    private val plantingId: Long? = route.plantingId
    private val initialPlotId: Long? = route.initialPlotId

    private val _uiState = MutableStateFlow(PlantingUiState())
    val uiState: StateFlow<PlantingUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val crops = cropRepository.getActiveOnly().first()
                val plots = plotRepository.getAll().first()

                if (plantingId != null) {
                    // Edit mode
                    val planting = plantingRepository.getById(plantingId)
                    val plotIds = plantingRepository.getPlotIdsForPlanting(plantingId)
                    val photos = plantingPhotoRepository.getByPlantingId(plantingId).first()
                    val crop = planting?.let { cropRepository.getById(it.cropId) }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = true,
                        crops = crops,
                        plots = plots,
                        selectedCrop = crop,
                        selectedPlotIds = plotIds.toSet(),
                        plantedDate = planting?.plantedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        note = planting?.note ?: "",
                        photos = photos,
                        existingPlanting = planting,
                    )
                } else {
                    // New planting mode
                    val initialPlots = if (initialPlotId != null) setOf(initialPlotId) else emptySet()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = false,
                        crops = crops,
                        plots = plots,
                        selectedPlotIds = initialPlots,
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

    fun selectCrop(crop: Crop) {
        _uiState.value = _uiState.value.copy(
            selectedCrop = crop,
            showCropSelector = false,
        )
    }

    fun togglePlotSelection(plotId: Long) {
        val currentSelection = _uiState.value.selectedPlotIds
        val newSelection = if (currentSelection.contains(plotId)) {
            currentSelection - plotId
        } else {
            currentSelection + plotId
        }
        _uiState.value = _uiState.value.copy(selectedPlotIds = newSelection)
    }

    fun setPlantedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            plantedDate = date,
            showDatePicker = false,
        )
    }

    fun setNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun addPhotoUri(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            pendingPhotoUris = _uiState.value.pendingPhotoUris + uri,
        )
    }

    fun removePhotoUri(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            pendingPhotoUris = _uiState.value.pendingPhotoUris - uri,
        )
    }

    fun removeExistingPhoto(photo: PlantingPhoto) {
        viewModelScope.launch {
            plantingPhotoRepository.delete(photo)
            _uiState.value = _uiState.value.copy(
                photos = _uiState.value.photos - photo,
            )
        }
    }

    fun showCropSelector() {
        _uiState.value = _uiState.value.copy(showCropSelector = true)
    }

    fun dismissCropSelector() {
        _uiState.value = _uiState.value.copy(showCropSelector = false)
    }

    fun showPlotSelector() {
        _uiState.value = _uiState.value.copy(showPlotSelector = true)
    }

    fun dismissPlotSelector() {
        _uiState.value = _uiState.value.copy(showPlotSelector = false)
    }

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun save(onPhotoSaved: suspend (Uri, Long) -> String?) {
        val state = _uiState.value
        val crop = state.selectedCrop
        val plotIds = state.selectedPlotIds.toList()

        if (crop == null) {
            _uiState.value = state.copy(errorMessage = "作物を選択してください")
            return
        }

        if (plotIds.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "区画を選択してください")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                val plantingIdResult: Long

                if (state.isEditMode && state.existingPlanting != null) {
                    // Update existing planting
                    val updatedPlanting = state.existingPlanting.copy(
                        cropId = crop.id,
                        plantedDate = state.plantedDate,
                        note = state.note.ifBlank { null },
                    )
                    plantingRepository.update(updatedPlanting)
                    plantingIdResult = state.existingPlanting.id
                } else {
                    // Create new planting
                    val newPlanting = Planting(
                        cropId = crop.id,
                        plantedDate = state.plantedDate,
                        note = state.note.ifBlank { null },
                        isActive = true,
                    )
                    plantingIdResult = plantingRepository.insert(newPlanting, plotIds)
                }

                // Save pending photos
                for (uri in state.pendingPhotoUris) {
                    val filePath = onPhotoSaved(uri, plantingIdResult)
                    if (filePath != null) {
                        val photo = PlantingPhoto(
                            plantingId = plantingIdResult,
                            filePath = filePath,
                            takenDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        )
                        plantingPhotoRepository.insert(photo)
                    }
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
        return state.selectedCrop != null && state.selectedPlotIds.isNotEmpty() && !state.isSaving
    }

    fun showHarvestDialog() {
        _uiState.value = _uiState.value.copy(showHarvestDialog = true)
    }

    fun dismissHarvestDialog() {
        _uiState.value = _uiState.value.copy(showHarvestDialog = false)
    }

    fun harvest(harvestDate: LocalDate) {
        val plantingId = this.plantingId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                plantingRepository.harvest(plantingId, harvestDate)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    showHarvestDialog = false,
                    harvestSuccess = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "収穫の記録に失敗しました: ${e.message}",
                )
            }
        }
    }

    fun canHarvest(): Boolean {
        val state = _uiState.value
        return state.isEditMode && state.existingPlanting?.isActive == true
    }
}
