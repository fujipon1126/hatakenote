package com.example.hatakenote.feature.plot

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.feature.plot.navigation.PlotPhotoRoute
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

data class PlotPhotoUiState(
    val isLoading: Boolean = true,
    val plotsWithPlantings: List<PlotWithCurrentPlanting> = emptyList(),
    val selectedPlotId: Long? = null,
    val photoDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val pendingPhotoUris: List<Uri> = emptyList(),
    val comment: String = "",
    val showDatePicker: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class PlotPhotoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plotRepository: PlotRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PlotPhotoRoute>()

    private val _uiState = MutableStateFlow(PlotPhotoUiState())
    val uiState: StateFlow<PlotPhotoUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val plotsWithPlantings = plotRepository.getAllWithCurrentPlantings().first()
                val initialDate = route.photoDate?.let {
                    try { LocalDate.parse(it) } catch (_: Exception) { null }
                } ?: Clock.System.todayIn(TimeZone.currentSystemDefault())

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    plotsWithPlantings = plotsWithPlantings,
                    selectedPlotId = route.plotId ?: plotsWithPlantings.firstOrNull()?.plot?.id,
                    photoDate = initialDate,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "データの読み込みに失敗しました",
                )
            }
        }
    }

    fun selectPlot(plotId: Long) {
        _uiState.value = _uiState.value.copy(selectedPlotId = plotId)
    }

    fun setPhotoDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            photoDate = date,
            showDatePicker = false,
        )
    }

    fun setComment(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
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

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun save(onPhotoSaved: suspend (Uri) -> String?) {
        val state = _uiState.value
        val plotId = state.selectedPlotId

        if (plotId == null) {
            _uiState.value = state.copy(errorMessage = "区画を選択してください")
            return
        }

        if (state.pendingPhotoUris.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "写真を選択してください")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                for (uri in state.pendingPhotoUris) {
                    val filePath = onPhotoSaved(uri)
                    if (filePath != null) {
                        val photo = PlantingPhoto(
                            plantingId = null,
                            plotId = plotId,
                            filePath = filePath,
                            takenDate = state.photoDate,
                            comment = state.comment.ifBlank { null },
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
        return state.selectedPlotId != null && state.pendingPhotoUris.isNotEmpty() && !state.isSaving
    }
}
