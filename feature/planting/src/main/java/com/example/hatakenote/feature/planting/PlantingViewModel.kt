package com.example.hatakenote.feature.planting

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.model.Harvest
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.EntityLastViewedRepository
import com.example.hatakenote.core.domain.repository.HarvestRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.ViewedEntityType
import com.example.hatakenote.core.domain.usecase.CheckRotationCompatibilityUseCase
import com.example.hatakenote.core.domain.usecase.GenerateRemindersUseCase
import com.example.hatakenote.core.domain.usecase.RotationWarning
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

/**
 * 追加予定の写真のメタデータ
 */
data class PendingPhotoMetadata(
    val uri: Uri,
    val takenDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val comment: String? = null,
)

data class PlantingUiState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val crops: List<Crop> = emptyList(),
    val cropFamilies: List<CropFamily> = emptyList(),
    val plots: List<Plot> = emptyList(),
    val selectedCrop: Crop? = null,
    val selectedPlotIds: Set<Long> = emptySet(),
    val plantedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val note: String = "",
    val photos: List<PlantingPhoto> = emptyList(),
    val pendingPhotos: List<PendingPhotoMetadata> = emptyList(),
    val existingPlanting: Planting? = null,
    val harvests: List<Harvest> = emptyList(),
    val rotationWarnings: List<RotationWarning> = emptyList(),
    val showCropSelector: Boolean = false,
    val showPlotSelector: Boolean = false,
    val showDatePicker: Boolean = false,
    val showHarvestDialog: Boolean = false,
    val selectedPhotoForDetail: PlantingPhoto? = null,
    val selectedPendingPhotoForDetail: PendingPhotoMetadata? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val harvestSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PlantingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cropRepository: CropRepository,
    private val cropFamilyRepository: CropFamilyRepository,
    private val plotRepository: PlotRepository,
    private val plantingRepository: PlantingRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
    private val harvestRepository: HarvestRepository,
    private val generateRemindersUseCase: GenerateRemindersUseCase,
    private val checkRotationCompatibilityUseCase: CheckRotationCompatibilityUseCase,
    private val entityLastViewedRepository: EntityLastViewedRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PlantingRoute>()
    private val plantingId: Long? = route.plantingId
    private val initialPlotId: Long? = route.initialPlotId

    private val _uiState = MutableStateFlow(PlantingUiState())
    val uiState: StateFlow<PlantingUiState> = _uiState.asStateFlow()

    init {
        loadData()
        if (plantingId != null) {
            viewModelScope.launch {
                entityLastViewedRepository.markViewed(ViewedEntityType.PLANTING, plantingId)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val crops = cropRepository.getActiveOnly().first()
                val cropFamilies = cropFamilyRepository.getAll().first()
                val plots = plotRepository.getAll().first()

                if (plantingId != null) {
                    // Edit mode
                    val planting = plantingRepository.getById(plantingId)
                    val plotIds = plantingRepository.getPlotIdsForPlanting(plantingId)
                    val plantingPhotos = plantingPhotoRepository.getByPlantingId(plantingId).first()
                    // 区画に紐づく写真も取得（カレンダーから登録された写真）
                    val plotPhotos = plotIds.flatMap { plotId ->
                        plantingPhotoRepository.getByPlotId(plotId).first()
                    }
                    // 重複を除いて統合（両方に紐づく写真がある場合）
                    val photos = (plantingPhotos + plotPhotos)
                        .distinctBy { it.id }
                        .sortedByDescending { it.takenDate }
                    val crop = planting?.let { cropRepository.getById(it.cropId) }
                    val harvests = harvestRepository.getByPlantingId(plantingId).first()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = true,
                        crops = crops,
                        cropFamilies = cropFamilies,
                        plots = plots,
                        selectedCrop = crop,
                        selectedPlotIds = plotIds.toSet(),
                        plantedDate = planting?.plantedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        note = planting?.note ?: "",
                        photos = photos,
                        existingPlanting = planting,
                        harvests = harvests,
                    )
                } else {
                    // New planting mode
                    val initialPlots = if (initialPlotId != null) setOf(initialPlotId) else emptySet()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isEditMode = false,
                        crops = crops,
                        cropFamilies = cropFamilies,
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
        checkRotationWarnings()
    }

    fun togglePlotSelection(plotId: Long) {
        val currentSelection = _uiState.value.selectedPlotIds
        val newSelection = if (currentSelection.contains(plotId)) {
            currentSelection - plotId
        } else {
            currentSelection + plotId
        }
        _uiState.value = _uiState.value.copy(selectedPlotIds = newSelection)
        checkRotationWarnings()
    }

    private fun checkRotationWarnings() {
        val state = _uiState.value
        val crop = state.selectedCrop
        val plotIds = state.selectedPlotIds.toList()

        if (crop == null || plotIds.isEmpty() || state.isEditMode) {
            _uiState.value = state.copy(rotationWarnings = emptyList())
            return
        }

        viewModelScope.launch {
            try {
                val warnings = checkRotationCompatibilityUseCase(crop, plotIds)
                _uiState.value = _uiState.value.copy(rotationWarnings = warnings)
            } catch (e: Exception) {
                // エラー時は警告なしとして処理を続行
                _uiState.value = _uiState.value.copy(rotationWarnings = emptyList())
            }
        }
    }

    fun dismissRotationWarnings() {
        _uiState.value = _uiState.value.copy(rotationWarnings = emptyList())
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
            pendingPhotos = _uiState.value.pendingPhotos + PendingPhotoMetadata(uri = uri),
        )
    }

    fun removePendingPhoto(pending: PendingPhotoMetadata) {
        _uiState.value = _uiState.value.copy(
            pendingPhotos = _uiState.value.pendingPhotos - pending,
            selectedPendingPhotoForDetail = null,
        )
    }

    fun removeExistingPhoto(photo: PlantingPhoto) {
        viewModelScope.launch {
            plantingPhotoRepository.delete(photo)
            _uiState.value = _uiState.value.copy(
                photos = _uiState.value.photos - photo,
                selectedPhotoForDetail = null,
            )
        }
    }

    fun showPhotoDetail(photo: PlantingPhoto) {
        _uiState.value = _uiState.value.copy(
            selectedPhotoForDetail = photo,
            selectedPendingPhotoForDetail = null,
        )
    }

    fun showPendingPhotoDetail(pending: PendingPhotoMetadata) {
        _uiState.value = _uiState.value.copy(
            selectedPendingPhotoForDetail = pending,
            selectedPhotoForDetail = null,
        )
    }

    fun dismissPhotoDetail() {
        _uiState.value = _uiState.value.copy(
            selectedPhotoForDetail = null,
            selectedPendingPhotoForDetail = null,
        )
    }

    fun updateExistingPhoto(photo: PlantingPhoto) {
        viewModelScope.launch {
            plantingPhotoRepository.update(photo)
            _uiState.value = _uiState.value.copy(
                photos = _uiState.value.photos.map { if (it.id == photo.id) photo else it },
            )
        }
    }

    fun updatePendingPhoto(pending: PendingPhotoMetadata) {
        _uiState.value = _uiState.value.copy(
            pendingPhotos = _uiState.value.pendingPhotos.map {
                if (it.uri == pending.uri) pending else it
            },
        )
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
                    plantingRepository.update(updatedPlanting, plotIds)
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

                    // 追肥スケジュールからリマインダーを自動生成
                    generateRemindersUseCase(
                        plantingId = plantingIdResult,
                        cropId = crop.id,
                        plantedDate = state.plantedDate,
                    )
                }

                // Save pending photos
                for (pending in state.pendingPhotos) {
                    val filePath = onPhotoSaved(pending.uri, plantingIdResult)
                    if (filePath != null) {
                        val photo = PlantingPhoto(
                            plantingId = plantingIdResult,
                            filePath = filePath,
                            takenDate = pending.takenDate,
                            comment = pending.comment,
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

    fun harvest(harvestDate: LocalDate, isFinal: Boolean) {
        val plantingId = this.plantingId ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                // 収穫記録を保存
                val harvest = Harvest(
                    plantingId = plantingId,
                    harvestedDate = harvestDate,
                )
                harvestRepository.insert(harvest)

                // 作付けの状態を更新
                plantingRepository.harvest(plantingId, harvestDate, isFinal)

                if (isFinal) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        showHarvestDialog = false,
                        harvestSuccess = true,
                    )
                } else {
                    // 継続収穫の場合は画面に留まり、収穫履歴を更新
                    val updatedHarvests = harvestRepository.getByPlantingId(plantingId).first()
                    val updatedPlanting = plantingRepository.getById(plantingId)
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        showHarvestDialog = false,
                        harvests = updatedHarvests,
                        existingPlanting = updatedPlanting,
                    )
                }
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
