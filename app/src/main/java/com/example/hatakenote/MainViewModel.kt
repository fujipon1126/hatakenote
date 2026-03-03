package com.example.hatakenote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.repository.AuthRepository
import com.example.hatakenote.core.domain.repository.FarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data object NotLoggedIn : MainUiState
    data object NoFarmSelected : MainUiState
    data object Ready : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    farmRepository: FarmRepository,
) : ViewModel() {

    val uiState = combine(
        authRepository.isSignedIn,
        farmRepository.getCurrentFarmId(),
    ) { isSignedIn, currentFarmId ->
        when {
            !isSignedIn -> MainUiState.NotLoggedIn
            currentFarmId == null -> MainUiState.NoFarmSelected
            else -> MainUiState.Ready
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState.Loading,
    )
}
