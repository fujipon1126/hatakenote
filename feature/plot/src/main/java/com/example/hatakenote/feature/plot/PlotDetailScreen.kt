package com.example.hatakenote.feature.plot

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.feature.plot.R
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlantingWithCrop
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.WorkType
import com.example.hatakenote.core.ui.util.parseColorSafe

@Composable
internal fun PlotDetailRoute(
    onBackClick: () -> Unit,
    onAddPlantingClick: (Long) -> Unit,
    onPlantingClick: (Long) -> Unit,
    onWorkLogClick: (Long?, Long?) -> Unit,
    viewModel: PlotDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PlotDetailScreen(
        uiState = uiState,
        plotId = viewModel.plotId,
        onBackClick = onBackClick,
        onAddPlantingClick = onAddPlantingClick,
        onPlantingClick = onPlantingClick,
        onWorkLogClick = onWorkLogClick,
        onEditClick = viewModel::showEditDialog,
        onDeleteClick = viewModel::showDeleteConfirmDialog,
        onDismissEditDialog = viewModel::dismissEditDialog,
        onDismissDeleteDialog = viewModel::dismissDeleteConfirmDialog,
        onUpdatePlot = viewModel::updatePlot,
        onConfirmDelete = { viewModel.deletePlot(onBackClick) },
        onDeletePlantingClick = viewModel::showDeletePlantingDialog,
        onDismissDeletePlantingDialog = viewModel::dismissDeletePlantingDialog,
        onConfirmDeletePlanting = viewModel::deletePlanting,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlotDetailScreen(
    uiState: PlotDetailUiState,
    plotId: Long,
    onBackClick: () -> Unit,
    onAddPlantingClick: (Long) -> Unit,
    onPlantingClick: (Long) -> Unit,
    onWorkLogClick: (Long?, Long?) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismissEditDialog: () -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onUpdatePlot: (String, PlotSide, Int) -> Unit,
    onConfirmDelete: () -> Unit,
    onDeletePlantingClick: (PlantingWithCrop) -> Unit = {},
    onDismissDeletePlantingDialog: () -> Unit = {},
    onConfirmDeletePlanting: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plot?.name ?: stringResource(R.string.plot_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.plot_detail_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.plot_detail_edit_plot)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.plot_detail_delete)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddPlantingClick(plotId) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.plot_detail_add_planting)
                )
            }
        }
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
        } else if (uiState.plot == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.plot_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // Plot Info Card
                PlotInfoCard(plot = uiState.plot)

                Spacer(modifier = Modifier.height(16.dp))

                // Current Plantings Section
                CurrentPlantingsSection(
                    plantings = uiState.currentPlantings,
                    onPlantingClick = onPlantingClick,
                    onDeletePlantingClick = onDeletePlantingClick,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Work Log Section (for plot-level work)
                WorkLogSection(
                    workLogs = uiState.workLogs,
                    plotId = plotId,
                    onAddWorkLogClick = { onWorkLogClick(null, plotId) },
                )
            }
        }

        // Edit Dialog
        if (uiState.showEditDialog && uiState.plot != null) {
            EditPlotDialog(
                plot = uiState.plot,
                errorMessage = uiState.editDialogError,
                onDismiss = onDismissEditDialog,
                onSave = onUpdatePlot,
            )
        }

        // Delete Planting Confirmation Dialog
        if (uiState.plantingToDelete != null) {
            DeletePlantingConfirmDialog(
                cropName = uiState.plantingToDelete.crop.name,
                onDismiss = onDismissDeletePlantingDialog,
                onConfirm = onConfirmDeletePlanting,
            )
        }

        // Delete Confirmation Dialog
        if (uiState.showDeleteConfirmDialog) {
            DeleteConfirmDialog(
                plotName = uiState.plot?.name ?: "",
                onDismiss = onDismissDeleteDialog,
                onConfirm = onConfirmDelete,
            )
        }
    }
}

@Composable
private fun PlotInfoCard(plot: Plot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.plot_detail_plot_info),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem(label = stringResource(R.string.plot_detail_position), value = "${plot.side.displayName()}${plot.number}")
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CurrentPlantingsSection(
    plantings: List<PlantingWithCrop>,
    onPlantingClick: (Long) -> Unit,
    onDeletePlantingClick: (PlantingWithCrop) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.plot_detail_current_crops),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (plantings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.plot_detail_no_crops),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            plantings.forEach { plantingWithCrop ->
                PlantingCard(
                    plantingWithCrop = plantingWithCrop,
                    onClick = { onPlantingClick(plantingWithCrop.planting.id) },
                    onDeleteClick = { onDeletePlantingClick(plantingWithCrop) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PlantingCard(
    plantingWithCrop: PlantingWithCrop,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val cropColor = parseColorSafe(plantingWithCrop.crop.colorHex, MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cropColor),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plantingWithCrop.crop.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.plot_planted_date, plantingWithCrop.planting.plantedDate.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.plot_detail_delete_planting),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun WorkLogSection(
    workLogs: List<WorkLog>,
    plotId: Long,
    onAddWorkLogClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.plot_detail_work_history),
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(onClick = onAddWorkLogClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.plot_detail_add_work),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.plot_detail_add_work))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Filter only plot-level work logs (TILL, BASE_FERTILIZE)
        val plotWorkLogs = workLogs.filter { it.workType.bindToPlot() }

        if (plotWorkLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.plot_detail_no_work_history),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            plotWorkLogs.take(5).forEach { workLog ->
                WorkLogItem(workLog = workLog)
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (plotWorkLogs.size > 5) {
                Text(
                    text = stringResource(R.string.plot_detail_more_items, plotWorkLogs.size - 5),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun WorkLogItem(workLog: WorkLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = getWorkTypeName(workLog.workType),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = workLog.workDate.toString(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            workLog.detail?.let { detail ->
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun getWorkTypeName(workType: WorkType): String = when (workType) {
    WorkType.TILL -> stringResource(R.string.work_type_till)
    WorkType.BASE_FERTILIZE -> stringResource(R.string.work_type_base_fertilize)
    WorkType.FERTILIZE -> stringResource(R.string.work_type_fertilize)
    WorkType.OTHER -> stringResource(R.string.work_type_other)
}

@Composable
private fun EditPlotDialog(
    plot: Plot,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (String, PlotSide, Int) -> Unit,
) {
    var selectedSide by remember { mutableStateOf(plot.side) }
    var number by remember { mutableStateOf(plot.number.toString()) }

    val autoName = "${selectedSide.displayName()}${number.toIntOrNull() ?: 1}"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.plot_detail_edit)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Side selection
                Text(
                    text = stringResource(R.string.plot_side),
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlotSide.entries.forEach { side ->
                        val isSelected = selectedSide == side
                        TextButton(
                            onClick = { selectedSide = side },
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(8.dp),
                                        )
                                    } else {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp),
                                        )
                                    }
                                ),
                        ) {
                            Text(
                                text = side.displayName(),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.plot_number)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.plot_name_preview, autoName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val n = number.toIntOrNull() ?: 1
                    onSave(autoName, selectedSide, n)
                },
                enabled = (number.toIntOrNull() ?: 0) >= 1,
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun DeletePlantingConfirmDialog(
    cropName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.plot_detail_delete_planting_title)) },
        text = {
            Text(stringResource(R.string.plot_detail_delete_planting_message, cropName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
private fun DeleteConfirmDialog(
    plotName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.plot_detail_delete_title)) },
        text = {
            Text(stringResource(R.string.plot_detail_delete_message, plotName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
