package com.example.hatakenote.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.repository.PlotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val plots: List<PlotWithCurrentPlanting> = emptyList(),
    val gridColumns: Int = 4,
    val gridRows: Int = 3,
    val isLoading: Boolean = true,
    val showAddPlotDialog: Boolean = false,
    val editingPlot: Plot? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val plotRepository: PlotRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPlots()
    }

    private fun loadPlots() {
        viewModelScope.launch {
            plotRepository.getAllWithCurrentPlantings()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
                .collect { plots ->
                    val maxPosition = plotRepository.getMaxGridPosition()
                    _uiState.value = _uiState.value.copy(
                        plots = plots,
                        gridColumns = maxOf(4, maxPosition.first + 1),
                        gridRows = maxOf(3, maxPosition.second + 1),
                        isLoading = false,
                    )
                }
        }
    }

    fun showAddPlotDialog() {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = true, editingPlot = null)
    }

    fun showEditPlotDialog(plot: Plot) {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = true, editingPlot = plot)
    }

    fun dismissPlotDialog() {
        _uiState.value = _uiState.value.copy(showAddPlotDialog = false, editingPlot = null)
    }

    fun savePlot(name: String, gridX: Int, gridY: Int, width: Int, height: Int) {
        viewModelScope.launch {
            val editingPlot = _uiState.value.editingPlot
            if (editingPlot != null) {
                plotRepository.update(
                    editingPlot.copy(
                        name = name,
                        gridX = gridX,
                        gridY = gridY,
                        width = width,
                        height = height,
                    )
                )
            } else {
                plotRepository.insert(
                    Plot(
                        name = name,
                        gridX = gridX,
                        gridY = gridY,
                        width = width,
                        height = height,
                    )
                )
            }
            dismissPlotDialog()
        }
    }

    fun deletePlot(plot: Plot) {
        viewModelScope.launch {
            plotRepository.delete(plot)
        }
    }
}
