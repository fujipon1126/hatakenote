package com.example.hatakenote.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.AppSettings
import com.example.hatakenote.core.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val latitude: String = "",
    val longitude: String = "",
    val locationName: String = "",
    val reminderNotifyDays: String = "",
    val showLocationDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            appSettingsRepository.getSettings().collect { settings ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    latitude = settings.latitude.toString(),
                    longitude = settings.longitude.toString(),
                    locationName = settings.locationName,
                    reminderNotifyDays = settings.reminderNotifyDaysBefore.toString(),
                )
            }
        }
    }

    fun showLocationDialog() {
        _uiState.value = _uiState.value.copy(showLocationDialog = true)
    }

    fun dismissLocationDialog() {
        _uiState.value = _uiState.value.copy(showLocationDialog = false)
    }

    fun updateLocation(latitude: String, longitude: String, locationName: String) {
        val lat = latitude.toDoubleOrNull()
        val lon = longitude.toDoubleOrNull()

        if (lat == null || lon == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "緯度・経度を正しく入力してください",
            )
            return
        }

        if (lat < -90 || lat > 90) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "緯度は-90〜90の範囲で入力してください",
            )
            return
        }

        if (lon < -180 || lon > 180) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "経度は-180〜180の範囲で入力してください",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                appSettingsRepository.updateLocation(lat, lon, locationName)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    showLocationDialog = false,
                    saveSuccess = true,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "保存に失敗しました",
                )
            }
        }
    }

    fun updateReminderNotifyDays(days: String) {
        val daysInt = days.toIntOrNull() ?: return

        if (daysInt < 0 || daysInt > 30) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "通知日数は0〜30の範囲で入力してください",
            )
            return
        }

        viewModelScope.launch {
            try {
                appSettingsRepository.updateReminderNotifyDays(daysInt)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "保存に失敗しました",
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
