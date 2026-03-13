package com.example.hatakenote.feature.crop

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.feature.crop.navigation.CropDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlantingWithPlots(
    val planting: Planting,
    val plotNames: List<String>,
    val photos: List<PlantingPhoto>,
)

data class CropDetailUiState(
    val crop: Crop? = null,
    val family: CropFamily? = null,
    val activePlantings: List<PlantingWithPlots> = emptyList(),
    val pastPlantings: List<PlantingWithPlots> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class CropDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cropRepository: CropRepository,
    private val cropFamilyRepository: CropFamilyRepository,
    private val plantingRepository: PlantingRepository,
    private val plotRepository: PlotRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CropDetailRoute>()
    private val cropId: Long = route.cropId

    private val _uiState = MutableStateFlow(CropDetailUiState())
    val uiState: StateFlow<CropDetailUiState> = _uiState.asStateFlow()

    init {
        loadCropDetails()
    }

    private fun loadCropDetails() {
        viewModelScope.launch {
            val crop = cropRepository.getById(cropId)
            if (crop == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            val family = cropFamilyRepository.getById(crop.familyId)
            val allPlantings = plantingRepository.getByCropId(cropId).first()

            val plantingsWithPlots = allPlantings.map { planting ->
                async { resolvePlantingWithPlots(planting) }
            }.awaitAll()

            _uiState.value = CropDetailUiState(
                crop = crop,
                family = family,
                activePlantings = plantingsWithPlots
                    .filter { it.planting.isActive }
                    .sortedByDescending { it.planting.plantedDate },
                pastPlantings = plantingsWithPlots
                    .filter { !it.planting.isActive }
                    .sortedByDescending { it.planting.harvestedDate ?: it.planting.plantedDate },
                isLoading = false,
            )
        }
    }

    private suspend fun resolvePlantingWithPlots(planting: Planting): PlantingWithPlots {
        val plotIds = plantingRepository.getPlotIdsForPlanting(planting.id)
        val plotNames = plotIds.mapNotNull { plotRepository.getById(it)?.name }
        val photos = plantingPhotoRepository.getByPlantingId(planting.id).first()
        return PlantingWithPlots(
            planting = planting,
            plotNames = plotNames,
            photos = photos,
        )
    }

    fun refreshData() {
        loadCropDetails()
    }
}
