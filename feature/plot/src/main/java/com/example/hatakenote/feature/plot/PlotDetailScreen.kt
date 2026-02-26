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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlantingWithCrop
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.WorkType

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
    onUpdatePlot: (String, Int, Int, Int, Int) -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.plot?.name ?: "区画詳細") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "編集"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "削除"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddPlantingClick(plotId) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "作付けを追加"
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
                    text = "区画が見つかりません",
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
                onDismiss = onDismissEditDialog,
                onSave = onUpdatePlot,
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
                text = "区画情報",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InfoItem(label = "位置", value = "(${plot.gridX}, ${plot.gridY})")
                InfoItem(label = "サイズ", value = "${plot.width} x ${plot.height}")
            }
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
) {
    Column {
        Text(
            text = "現在の作物",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (plantings.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "現在作付けされている作物はありません",
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
) {
    val cropColor = try {
        Color(android.graphics.Color.parseColor(plantingWithCrop.crop.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    text = "植付: ${plantingWithCrop.planting.plantedDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                text = "区画作業履歴",
                style = MaterialTheme.typography.titleMedium,
            )
            OutlinedButton(onClick = onAddWorkLogClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("作業追加")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Filter only plot-level work logs (TILL, BASE_FERTILIZE)
        val plotWorkLogs = workLogs.filter { it.workType.bindToPlot() }

        if (plotWorkLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "作業履歴はありません",
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
                    text = "他 ${plotWorkLogs.size - 5} 件",
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
                text = workLog.workType.toDisplayName(),
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

private fun WorkType.toDisplayName(): String = when (this) {
    WorkType.TILL -> "耕起"
    WorkType.BASE_FERTILIZE -> "元肥"
    WorkType.FERTILIZE -> "追肥"
    WorkType.OTHER -> "その他"
}

@Composable
private fun EditPlotDialog(
    plot: Plot,
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int, Int) -> Unit,
) {
    var name by remember { mutableStateOf(plot.name) }
    var gridX by remember { mutableStateOf(plot.gridX.toString()) }
    var gridY by remember { mutableStateOf(plot.gridY.toString()) }
    var width by remember { mutableStateOf(plot.width.toString()) }
    var height by remember { mutableStateOf(plot.height.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("区画を編集") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("区画名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = gridX,
                        onValueChange = { gridX = it.filter { c -> c.isDigit() } },
                        label = { Text("X座標") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = gridY,
                        onValueChange = { gridY = it.filter { c -> c.isDigit() } },
                        label = { Text("Y座標") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = width,
                        onValueChange = { width = it.filter { c -> c.isDigit() } },
                        label = { Text("幅") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it.filter { c -> c.isDigit() } },
                        label = { Text("高さ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val x = gridX.toIntOrNull() ?: 0
                    val y = gridY.toIntOrNull() ?: 0
                    val w = maxOf(1, width.toIntOrNull() ?: 1)
                    val h = maxOf(1, height.toIntOrNull() ?: 1)
                    if (name.isNotBlank()) {
                        onSave(name, x, y, w, h)
                    }
                },
                enabled = name.isNotBlank(),
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
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
        title = { Text("区画を削除") },
        text = {
            Text("「$plotName」を削除しますか？この操作は取り消せません。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("削除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}
