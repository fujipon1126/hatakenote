package com.example.hatakenote.feature.worklog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.feature.worklog.R
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.WorkType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun WorkLogRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: WorkLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved()
        }
    }

    WorkLogScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onWorkTypeSelected = viewModel::selectWorkType,
        onDateClick = viewModel::showDatePicker,
        onDateSelected = viewModel::setWorkDate,
        onDatePickerDismiss = viewModel::dismissDatePicker,
        onDetailChanged = viewModel::setDetail,
        onPlantingSelectorClick = viewModel::showPlantingSelector,
        onPlantingSelected = viewModel::selectPlanting,
        onPlantingSelectorDismiss = viewModel::dismissPlantingSelector,
        onPlotSelectorClick = viewModel::showPlotSelector,
        onPlotSelected = viewModel::selectPlot,
        onPlotSelectorDismiss = viewModel::dismissPlotSelector,
        onSaveClick = viewModel::save,
        canSave = viewModel.canSave(),
        getWorkTypeLabel = viewModel::getWorkTypeLabel,
        getDetailPlaceholder = viewModel::getDetailPlaceholder,
        onErrorDismiss = viewModel::clearError,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun WorkLogScreen(
    uiState: WorkLogUiState,
    onBackClick: () -> Unit,
    onWorkTypeSelected: (WorkType) -> Unit,
    onDateClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDatePickerDismiss: () -> Unit,
    onDetailChanged: (String) -> Unit,
    onPlantingSelectorClick: () -> Unit,
    onPlantingSelected: (PlantingWithCropAndPlots) -> Unit,
    onPlantingSelectorDismiss: () -> Unit,
    onPlotSelectorClick: () -> Unit,
    onPlotSelected: (Plot) -> Unit,
    onPlotSelectorDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    canSave: Boolean,
    getWorkTypeLabel: (WorkType) -> String,
    getDetailPlaceholder: () -> String,
    onErrorDismiss: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) stringResource(R.string.worklog_edit) else stringResource(R.string.worklog_add))
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.worklog_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = canSave,
                    ) {
                        Icon(Icons.Filled.Check, stringResource(R.string.save))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Work Type Selection
                item {
                    Text(
                        text = stringResource(R.string.worklog_type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WorkType.entries.forEach { workType ->
                            FilterChip(
                                selected = uiState.selectedWorkType == workType,
                                onClick = { onWorkTypeSelected(workType) },
                                label = { Text(getWorkTypeLabel(workType)) },
                            )
                        }
                    }
                }

                // Planting or Plot Selection based on work type
                item {
                    if (uiState.selectedWorkType.bindToPlanting()) {
                        // Planting selection
                        Text(
                            text = stringResource(R.string.worklog_target_planting),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onPlantingSelectorClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isPlantingLocked,
                        ) {
                            Text(
                                text = uiState.selectedPlanting?.let { planting ->
                                    val plotNames = planting.plots.joinToString(", ") { it.name }
                                    "${planting.crop.name}（${plotNames}）"
                                } ?: stringResource(R.string.worklog_select_planting),
                            )
                        }
                    } else {
                        // Plot selection
                        Text(
                            text = stringResource(R.string.worklog_target_plot),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onPlotSelectorClick,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isPlotLocked,
                        ) {
                            Text(
                                text = uiState.selectedPlot?.name ?: stringResource(R.string.worklog_select_plot),
                            )
                        }
                    }
                }

                // Date Selection
                item {
                    Text(
                        text = stringResource(R.string.worklog_date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onDateClick,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.worklog_select_date))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.worklog_date_format, uiState.workDate.year, uiState.workDate.monthNumber, uiState.workDate.dayOfMonth))
                    }
                }

                // Detail Input
                item {
                    Text(
                        text = stringResource(R.string.worklog_detail),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.detail,
                        onValueChange = onDetailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(getDetailPlaceholder()) },
                        minLines = 3,
                        maxLines = 5,
                    )
                }
            }
        }
    }

    // Date Picker Dialog
    if (uiState.showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.workDate
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds(),
        )
        DatePickerDialog(
            onDismissRequest = onDatePickerDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                            onDateSelected(localDate)
                        }
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDatePickerDismiss) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Planting Selector Dialog
    if (uiState.showPlantingSelector) {
        AlertDialog(
            onDismissRequest = onPlantingSelectorDismiss,
            title = { Text(stringResource(R.string.worklog_select_planting)) },
            text = {
                if (uiState.availablePlantings.isEmpty()) {
                    Text(stringResource(R.string.worklog_no_plantings))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.availablePlantings) { planting ->
                            PlantingSelectItem(
                                planting = planting,
                                isSelected = uiState.selectedPlanting?.planting?.id == planting.planting.id,
                                onClick = { onPlantingSelected(planting) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onPlantingSelectorDismiss) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }

    // Plot Selector Dialog
    if (uiState.showPlotSelector) {
        AlertDialog(
            onDismissRequest = onPlotSelectorDismiss,
            title = { Text(stringResource(R.string.worklog_select_plot)) },
            text = {
                if (uiState.availablePlots.isEmpty()) {
                    Text(stringResource(R.string.worklog_no_plots))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.availablePlots) { plot ->
                            PlotSelectItem(
                                plot = plot,
                                isSelected = uiState.selectedPlot?.id == plot.id,
                                onClick = { onPlotSelected(plot) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onPlotSelectorDismiss) {
                    Text(stringResource(R.string.close))
                }
            },
        )
    }
}

@Composable
private fun PlantingSelectItem(
    planting: PlantingWithCropAndPlots,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val cropColor = try {
        Color(planting.crop.colorHex.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                cropColor.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = planting.crop.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = cropColor,
                )
                val plotNames = planting.plots.joinToString(", ") { it.name }
                Text(
                    text = plotNames,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.worklog_planted_date, planting.planting.plantedDate.year, planting.planting.plantedDate.monthNumber, planting.planting.plantedDate.dayOfMonth),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.worklog_selected),
                    tint = cropColor,
                )
            }
        }
    }
}

@Composable
private fun PlotSelectItem(
    plot: Plot,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = plot.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.worklog_selected),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
