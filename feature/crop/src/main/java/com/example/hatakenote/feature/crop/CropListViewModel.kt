package com.example.hatakenote.feature.crop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.repository.CropFamilyRepository
import com.example.hatakenote.core.domain.repository.CropRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CropFamilyGroup(
    val family: CropFamily?,
    val crops: List<Crop>,
)

data class CropListUiState(
    val crops: List<Crop> = emptyList(),
    val families: List<CropFamily> = emptyList(),
    val groupedCrops: List<CropFamilyGroup> = emptyList(),
    val isLoading: Boolean = true,
    val editingCrop: Crop? = null,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val deletingCrop: Crop? = null,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class CropListViewModel @Inject constructor(
    private val cropRepository: CropRepository,
    private val cropFamilyRepository: CropFamilyRepository,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<CropListUiState> = combine(
        cropRepository.getAll(),
        cropFamilyRepository.getAll(),
        _dialogState,
    ) { crops, families, dialogState ->
        val groupedCrops = buildList {
            families.forEach { family ->
                val cropsInFamily = crops.filter { it.familyId == family.id }
                if (cropsInFamily.isNotEmpty()) {
                    add(CropFamilyGroup(family = family, crops = cropsInFamily))
                }
            }
            val orphanCrops = crops.filter { crop -> families.none { it.id == crop.familyId } }
            if (orphanCrops.isNotEmpty()) {
                add(CropFamilyGroup(family = null, crops = orphanCrops))
            }
        }
        CropListUiState(
            crops = crops,
            families = families,
            groupedCrops = groupedCrops,
            isLoading = false,
            editingCrop = dialogState.editingCrop,
            showEditDialog = dialogState.showEditDialog,
            showDeleteConfirmDialog = dialogState.showDeleteConfirmDialog,
            deletingCrop = dialogState.deletingCrop,
            errorMessage = dialogState.errorMessage,
            saveSuccess = dialogState.saveSuccess,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CropListUiState(),
    )

    fun showAddDialog() {
        _dialogState.update {
            it.copy(
                showEditDialog = true,
                editingCrop = null,
            )
        }
    }

    fun showEditDialog(crop: Crop) {
        _dialogState.update {
            it.copy(
                showEditDialog = true,
                editingCrop = crop,
            )
        }
    }

    fun dismissEditDialog() {
        _dialogState.update {
            it.copy(
                showEditDialog = false,
                editingCrop = null,
            )
        }
    }

    fun saveCrop(name: String, familyId: Long, colorHex: String) {
        viewModelScope.launch {
            try {
                val currentCrop = _dialogState.value.editingCrop
                if (currentCrop != null) {
                    // Update existing crop
                    cropRepository.update(
                        currentCrop.copy(
                            name = name,
                            familyId = familyId,
                            colorHex = colorHex,
                        )
                    )
                } else {
                    // Insert new crop
                    cropRepository.insert(
                        Crop(
                            name = name,
                            familyId = familyId,
                            colorHex = colorHex,
                        )
                    )
                }
                _dialogState.update {
                    it.copy(
                        showEditDialog = false,
                        editingCrop = null,
                        saveSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(errorMessage = "保存に失敗しました: ${e.message}")
                }
            }
        }
    }

    fun showDeleteConfirmDialog(crop: Crop) {
        _dialogState.update {
            it.copy(
                showDeleteConfirmDialog = true,
                deletingCrop = crop,
            )
        }
    }

    fun dismissDeleteConfirmDialog() {
        _dialogState.update {
            it.copy(
                showDeleteConfirmDialog = false,
                deletingCrop = null,
            )
        }
    }

    fun deleteCrop() {
        val crop = _dialogState.value.deletingCrop ?: return
        viewModelScope.launch {
            try {
                cropRepository.delete(crop)
                _dialogState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        deletingCrop = null,
                        saveSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        deletingCrop = null,
                        errorMessage = "削除に失敗しました: ${e.message}",
                    )
                }
            }
        }
    }

    fun toggleCropActive(crop: Crop) {
        viewModelScope.launch {
            try {
                cropRepository.update(crop.copy(isActive = !crop.isActive))
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(errorMessage = "更新に失敗しました: ${e.message}")
                }
            }
        }
    }

    fun clearError() {
        _dialogState.update { it.copy(errorMessage = null) }
    }

    fun clearSaveSuccess() {
        _dialogState.update { it.copy(saveSuccess = false) }
    }

    private data class DialogState(
        val showEditDialog: Boolean = false,
        val editingCrop: Crop? = null,
        val showDeleteConfirmDialog: Boolean = false,
        val deletingCrop: Crop? = null,
        val errorMessage: String? = null,
        val saveSuccess: Boolean = false,
    )
}
