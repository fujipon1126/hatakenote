package com.example.hatakenote.feature.fertilizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Fertilizer
import com.example.hatakenote.core.domain.repository.FertilizerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FertilizerListUiState(
    val fertilizers: List<Fertilizer> = emptyList(),
    val isLoading: Boolean = true,
    val editingFertilizer: Fertilizer? = null,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val deletingFertilizer: Fertilizer? = null,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class FertilizerListViewModel @Inject constructor(
    private val fertilizerRepository: FertilizerRepository,
) : ViewModel() {

    private val _dialogState = MutableStateFlow(DialogState())

    val uiState: StateFlow<FertilizerListUiState> = combine(
        fertilizerRepository.getAll(),
        _dialogState,
    ) { fertilizers, dialogState ->
        FertilizerListUiState(
            fertilizers = fertilizers,
            isLoading = false,
            editingFertilizer = dialogState.editingFertilizer,
            showEditDialog = dialogState.showEditDialog,
            showDeleteConfirmDialog = dialogState.showDeleteConfirmDialog,
            deletingFertilizer = dialogState.deletingFertilizer,
            errorMessage = dialogState.errorMessage,
            saveSuccess = dialogState.saveSuccess,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FertilizerListUiState(),
    )

    fun showAddDialog() {
        _dialogState.update {
            it.copy(showEditDialog = true, editingFertilizer = null)
        }
    }

    fun showEditDialog(fertilizer: Fertilizer) {
        _dialogState.update {
            it.copy(showEditDialog = true, editingFertilizer = fertilizer)
        }
    }

    fun dismissEditDialog() {
        _dialogState.update {
            it.copy(showEditDialog = false, editingFertilizer = null)
        }
    }

    fun saveFertilizer(name: String, defaultAmount: String, note: String) {
        viewModelScope.launch {
            try {
                val current = _dialogState.value.editingFertilizer
                if (current != null) {
                    fertilizerRepository.update(
                        current.copy(
                            name = name,
                            defaultAmount = defaultAmount,
                            note = note,
                        )
                    )
                } else {
                    fertilizerRepository.insert(
                        Fertilizer(
                            name = name,
                            defaultAmount = defaultAmount,
                            note = note,
                        )
                    )
                }
                _dialogState.update {
                    it.copy(
                        showEditDialog = false,
                        editingFertilizer = null,
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

    fun showDeleteConfirmDialog(fertilizer: Fertilizer) {
        _dialogState.update {
            it.copy(showDeleteConfirmDialog = true, deletingFertilizer = fertilizer)
        }
    }

    fun dismissDeleteConfirmDialog() {
        _dialogState.update {
            it.copy(showDeleteConfirmDialog = false, deletingFertilizer = null)
        }
    }

    fun deleteFertilizer() {
        val fertilizer = _dialogState.value.deletingFertilizer ?: return
        viewModelScope.launch {
            try {
                fertilizerRepository.delete(fertilizer)
                _dialogState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        deletingFertilizer = null,
                        saveSuccess = true,
                    )
                }
            } catch (e: Exception) {
                _dialogState.update {
                    it.copy(
                        showDeleteConfirmDialog = false,
                        deletingFertilizer = null,
                        errorMessage = "削除に失敗しました: ${e.message}",
                    )
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
        val editingFertilizer: Fertilizer? = null,
        val showDeleteConfirmDialog: Boolean = false,
        val deletingFertilizer: Fertilizer? = null,
        val errorMessage: String? = null,
        val saveSuccess: Boolean = false,
    )
}
