package com.example.hatakenote.feature.farm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Farm
import com.example.hatakenote.core.domain.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

data class FarmUiState(
    val farms: List<Farm> = emptyList(),
    val selectedFarmId: String? = null,
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val isJoining: Boolean = false,
    val isDeleting: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showJoinDialog: Boolean = false,
    val showInviteDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val farmToDelete: Farm? = null,
    val currentInviteCode: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val farmSelected: Boolean = false,
)

@HiltViewModel
class FarmViewModel @Inject constructor(
    private val farmRepository: FarmRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    init {
        loadFarms()
        observeCurrentFarmId()
    }

    private fun loadFarms() {
        viewModelScope.launch {
            farmRepository.getFarms()
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = getErrorMessage(e),
                        )
                    }
                }
                .collect { farms ->
                    _uiState.update {
                        it.copy(
                            farms = farms,
                            isLoading = false,
                        )
                    }
                }
        }
    }

    private fun observeCurrentFarmId() {
        viewModelScope.launch {
            farmRepository.getCurrentFarmId().collect { farmId ->
                _uiState.update { it.copy(selectedFarmId = farmId) }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun hideCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun showJoinDialog() {
        _uiState.update { it.copy(showJoinDialog = true) }
    }

    fun hideJoinDialog() {
        _uiState.update { it.copy(showJoinDialog = false) }
    }

    fun showInviteDialog(farm: Farm) {
        _uiState.update {
            it.copy(
                showInviteDialog = true,
                currentInviteCode = farm.inviteCode,
            )
        }
    }

    fun hideInviteDialog() {
        _uiState.update {
            it.copy(
                showInviteDialog = false,
                currentInviteCode = null,
            )
        }
    }

    fun showDeleteDialog(farm: Farm) {
        _uiState.update {
            it.copy(
                showDeleteDialog = true,
                farmToDelete = farm,
            )
        }
    }

    fun hideDeleteDialog() {
        _uiState.update {
            it.copy(
                showDeleteDialog = false,
                farmToDelete = null,
            )
        }
    }

    fun createFarm(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }

            farmRepository.createFarm(name)
                .onSuccess { farm ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            successMessage = "畑「${farm.name}」を作成しました",
                            farmSelected = true,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = getErrorMessage(e),
                        )
                    }
                }
        }
    }

    fun joinFarm(inviteCode: String) {
        if (inviteCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }

            farmRepository.joinFarm(inviteCode.uppercase())
                .onSuccess { farm ->
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            showJoinDialog = false,
                            successMessage = "畑「${farm.name}」に参加しました",
                            farmSelected = true,
                        )
                    }
                }
                .onFailure { e ->
                    val errorMsg = if (e.message?.contains("Invalid invite code") == true) {
                        "招待コードが見つかりません"
                    } else {
                        getErrorMessage(e)
                    }
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            errorMessage = errorMsg,
                        )
                    }
                }
        }
    }

    fun selectFarm(farm: Farm) {
        viewModelScope.launch {
            farmRepository.setCurrentFarmId(farm.id)
            _uiState.update { it.copy(farmSelected = true) }
        }
    }

    fun deleteFarm(farm: Farm) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null) }

            farmRepository.deleteFarm(farm.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            farmToDelete = null,
                            successMessage = "畑「${farm.name}」を削除しました",
                        )
                    }
                }
                .onFailure { e ->
                    val errorMsg = if (e.message?.contains("Only the owner") == true) {
                        "オーナーのみが畑を削除できます"
                    } else {
                        getErrorMessage(e)
                    }
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            showDeleteDialog = false,
                            farmToDelete = null,
                            errorMessage = errorMsg,
                        )
                    }
                }
        }
    }

    fun regenerateInviteCode(farmId: String) {
        viewModelScope.launch {
            farmRepository.generateInviteCode(farmId)
                .onSuccess { newCode ->
                    _uiState.update {
                        it.copy(
                            currentInviteCode = newCode,
                            successMessage = "新しい招待コードを生成しました",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(errorMessage = getErrorMessage(e))
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearFarmSelected() {
        _uiState.update { it.copy(farmSelected = false) }
    }

    private fun getErrorMessage(e: Throwable): String {
        return when (e) {
            is UnknownHostException -> "ネットワークエラーが発生しました。接続を確認してください。"
            else -> e.message ?: "エラーが発生しました。もう一度お試しください。"
        }
    }
}
