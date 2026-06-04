package com.example.hatakenote.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hatakenote.feature.home.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.DailyForecast
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.model.Weather
import com.example.hatakenote.core.domain.model.WeatherCode
import com.example.hatakenote.core.ui.component.NewLabel
import com.example.hatakenote.core.ui.util.contrastTextColor
import com.example.hatakenote.core.ui.util.newBadgeColors
import com.example.hatakenote.core.ui.util.parseColorSafe
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

@Composable
internal fun HomeRoute(
    onPlotClick: (Long) -> Unit,
    onNavigateToFarmSelect: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onPlotClick = onPlotClick,
        onNavigateToFarmSelect = onNavigateToFarmSelect,
        onAddPlotClick = viewModel::showAddPlotDialog,
        onDismissPlotDialog = viewModel::dismissPlotDialog,
        onSavePlot = viewModel::savePlot,
        onCompleteReminder = viewModel::completeReminder,
        onRefreshWeather = viewModel::refreshWeather,
        onClearError = viewModel::clearError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onPlotClick: (Long) -> Unit,
    onNavigateToFarmSelect: () -> Unit,
    onAddPlotClick: () -> Unit,
    onDismissPlotDialog: () -> Unit,
    onSavePlot: (String, PlotSide, Int) -> Unit,
    onCompleteReminder: (Long) -> Unit,
    onRefreshWeather: () -> Unit,
    onClearError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorId) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.farmName.ifEmpty { stringResource(R.string.home_title) }
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToFarmSelect) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = stringResource(R.string.home_switch_farm),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlotClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_add_plot)
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                // 天気セクション
                if (uiState.weather != null) {
                    WeatherSection(
                        weather = uiState.weather,
                        locationName = uiState.weatherLocationName,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                } else if (uiState.weatherError) {
                    WeatherErrorSection(onRetry = onRefreshWeather)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // リマインダーセクション
                if (uiState.upcomingReminders.isNotEmpty()) {
                    ReminderSection(
                        reminders = uiState.upcomingReminders,
                        onCompleteReminder = onCompleteReminder,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Text(
                    text = stringResource(R.string.home_plot_map),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                if (uiState.plots.isEmpty()) {
                    EmptyPlotMessage(onAddPlotClick = onAddPlotClick)
                } else {
                    PlotGrid(
                        plots = uiState.plots,
                        newBadgePlotIds = uiState.newBadgePlotIds,
                        onPlotClick = onPlotClick,
                    )
                }
            }
        }

        if (uiState.showAddPlotDialog) {
            AddEditPlotDialog(
                editingPlot = uiState.editingPlot,
                errorMessage = uiState.plotDialogError,
                onDismiss = onDismissPlotDialog,
                onSave = onSavePlot,
            )
        }
    }
}

@Composable
private fun EmptyPlotMessage(onAddPlotClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_no_plots),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_no_plots_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 隣接する同じ作物の区画をマージしたグループ
 */
private data class MergedPlotGroup(
    val plots: List<PlotWithCurrentPlanting>,
    val startRow: Int,
    val startCol: Int,
    val rowSpan: Int,
    val colSpan: Int,
)

/**
 * 隣接する同じ作物の区画をグループ化する。
 * 縦方向を優先してマージし、次に横方向のマージを試みる。
 */
private fun computeMergedGroups(
    plots: List<PlotWithCurrentPlanting>,
    maxNumber: Int,
): Pair<List<MergedPlotGroup>, List<Pair<Int, Int>>> {
    val grid = Array(maxNumber) { arrayOfNulls<PlotWithCurrentPlanting>(2) }
    for (p in plots) {
        val row = p.plot.number - 1
        val col = if (p.plot.side == PlotSide.LEFT) 0 else 1
        if (row in 0 until maxNumber) grid[row][col] = p
    }

    fun cropKey(p: PlotWithCurrentPlanting?): Set<Long>? {
        if (p == null || p.currentPlantings.isEmpty()) return null
        return p.currentPlantings.map { it.crop.id }.toSortedSet()
    }

    val visited = Array(maxNumber) { BooleanArray(2) }
    val groups = mutableListOf<MergedPlotGroup>()
    val emptyCells = mutableListOf<Pair<Int, Int>>()

    for (row in 0 until maxNumber) {
        for (col in 0..1) {
            if (visited[row][col]) continue
            val plot = grid[row][col]
            if (plot == null) {
                emptyCells.add(row to col)
                continue
            }

            visited[row][col] = true
            val key = cropKey(plot)

            if (key == null) {
                groups.add(MergedPlotGroup(listOf(plot), row, col, 1, 1))
                continue
            }

            // 縦方向に拡張
            var maxRow = row
            for (r in row + 1 until maxNumber) {
                if (!visited[r][col] && cropKey(grid[r][col]) == key) {
                    maxRow = r
                } else break
            }

            // 横方向に拡張（縦の全範囲が同じ作物の場合のみ）
            var maxCol = col
            if (col == 0) {
                var canExpandRight = true
                for (r in row..maxRow) {
                    if (visited[r][1] || cropKey(grid[r][1]) != key) {
                        canExpandRight = false
                        break
                    }
                }
                if (canExpandRight) maxCol = 1
            }

            val mergedPlots = mutableListOf<PlotWithCurrentPlanting>()
            for (r in row..maxRow) {
                for (c in col..maxCol) {
                    visited[r][c] = true
                    grid[r][c]?.let { mergedPlots.add(it) }
                }
            }

            groups.add(
                MergedPlotGroup(mergedPlots, row, col, maxRow - row + 1, maxCol - col + 1),
            )
        }
    }

    return groups to emptyCells
}

@Composable
private fun PlotGrid(
    plots: List<PlotWithCurrentPlanting>,
    newBadgePlotIds: Set<Long>,
    onPlotClick: (Long) -> Unit,
) {
    val gap = 8.dp
    val maxNumber = plots.maxOfOrNull { it.plot.number } ?: return

    val (mergedGroups, emptyCells) = remember(plots) {
        computeMergedGroups(plots, maxNumber)
    }

    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        // Header row
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "左",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "右",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellWidth = (maxWidth - gap) / 2
            val cellHeight = cellWidth / 1.6f
            val totalHeight = cellHeight * maxNumber + gap * (maxNumber - 1)

            Box(modifier = Modifier.fillMaxWidth().height(totalHeight)) {
                // 区画のないセル
                for ((row, col) in emptyCells) {
                    EmptyCell(
                        modifier = Modifier
                            .offset(
                                x = if (col == 0) 0.dp else cellWidth + gap,
                                y = (cellHeight + gap) * row,
                            )
                            .size(width = cellWidth, height = cellHeight),
                    )
                }

                // マージされたグループと単独セル
                for (group in mergedGroups) {
                    val x = if (group.startCol == 0) 0.dp else cellWidth + gap
                    val y = (cellHeight + gap) * group.startRow
                    val w = cellWidth * group.colSpan + gap * (group.colSpan - 1)
                    val h = cellHeight * group.rowSpan + gap * (group.rowSpan - 1)

                    if (group.plots.size == 1) {
                        PlotCell(
                            plotWithPlanting = group.plots.first(),
                            isNew = group.plots.first().plot.id in newBadgePlotIds,
                            modifier = Modifier
                                .offset(x = x, y = y)
                                .size(width = w, height = h),
                            onClick = { onPlotClick(group.plots.first().plot.id) },
                        )
                    } else {
                        MergedPlotCell(
                            group = group,
                            isNew = group.plots.any { it.plot.id in newBadgePlotIds },
                            modifier = Modifier
                                .offset(x = x, y = y)
                                .size(width = w, height = h),
                            onClick = { onPlotClick(group.plots.first().plot.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlotCell(
    plotWithPlanting: PlotWithCurrentPlanting,
    isNew: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val plot = plotWithPlanting.plot
    val currentPlantings = plotWithPlanting.currentPlantings

    val backgroundColor = if (currentPlantings.isNotEmpty()) {
        parseColorSafe(currentPlantings.first().crop.colorHex, MaterialTheme.colorScheme.primaryContainer)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (currentPlantings.isNotEmpty()) {
        contrastTextColor(backgroundColor)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = plot.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isNew) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val (badgeBg, badgeFg) = newBadgeColors(backgroundColor)
                    NewLabel(
                        label = stringResource(R.string.home_new_badge),
                        containerColor = badgeBg,
                        contentColor = badgeFg,
                    )
                }
            }
            if (currentPlantings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                currentPlantings.take(2).forEach { plantingWithCrop ->
                    Text(
                        text = plantingWithCrop.crop.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = textColor.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (currentPlantings.size > 2) {
                    Text(
                        text = "+${currentPlantings.size - 2}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = textColor.copy(alpha = 0.7f),
                    )
                }
            } else {
                Text(
                    text = "空き",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
private fun MergedPlotCell(
    group: MergedPlotGroup,
    isNew: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val firstPlot = group.plots.first()
    val currentPlantings = firstPlot.currentPlantings

    val backgroundColor = parseColorSafe(
        currentPlantings.first().crop.colorHex,
        MaterialTheme.colorScheme.primaryContainer,
    )
    val textColor = contrastTextColor(backgroundColor)

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = group.plots.joinToString(" · ") { it.plot.name },
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isNew) {
                    Spacer(modifier = Modifier.width(4.dp))
                    val (badgeBg, badgeFg) = newBadgeColors(backgroundColor)
                    NewLabel(
                        label = stringResource(R.string.home_new_badge),
                        containerColor = badgeBg,
                        contentColor = badgeFg,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            // 作物名（グループ内で共通）
            currentPlantings.take(2).forEach { plantingWithCrop ->
                Text(
                    text = plantingWithCrop.crop.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = textColor.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (currentPlantings.size > 2) {
                Text(
                    text = "+${currentPlantings.size - 2}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = textColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun EmptyCell(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
            ),
    )
}

@Composable
private fun AddEditPlotDialog(
    editingPlot: Plot?,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onSave: (String, PlotSide, Int) -> Unit,
) {
    var selectedSide by remember { mutableStateOf(editingPlot?.side ?: PlotSide.LEFT) }
    var number by remember { mutableStateOf(editingPlot?.number?.toString() ?: "1") }

    // Auto-generate name from side + number
    val autoName = "${selectedSide.displayName()}${number.toIntOrNull() ?: 1}"

    val isEdit = editingPlot != null
    val title = if (isEdit) stringResource(R.string.home_edit_plot) else stringResource(R.string.home_add_plot)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Side selection (left/right)
                Text(
                    text = stringResource(R.string.home_plot_side),
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

                // Number input
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.home_plot_number)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Auto-generated name preview
                Text(
                    text = stringResource(R.string.home_plot_name_preview, autoName),
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
private fun ReminderSection(
    reminders: List<Reminder>,
    onCompleteReminder: (Long) -> Unit,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = stringResource(R.string.home_upcoming_reminders),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.home_upcoming_reminders),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            reminders.forEach { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onComplete = { onCompleteReminder(reminder.id) },
                )
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onComplete: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val daysUntil = today.daysUntil(reminder.scheduledDate)

    val dateText = when {
        daysUntil < 0 -> "${-daysUntil}日超過"
        daysUntil == 0 -> "今日"
        daysUntil == 1 -> "明日"
        else -> "${daysUntil}日後"
    }

    val isOverdue = daysUntil < 0
    val containerColor = when {
        isOverdue -> MaterialTheme.colorScheme.errorContainer
        daysUntil <= 1 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reminder.scheduledDate.monthNumber}/${reminder.scheduledDate.dayOfMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = stringResource(R.string.complete),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun WeatherErrorSection(
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = stringResource(R.string.weather_error_title),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.weather_error_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = stringResource(R.string.weather_error_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                )
            }
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
private fun WeatherSection(
    weather: Weather,
    locationName: String,
) {
    Column {
        // 現在の天気
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Icon(
                imageVector = getWeatherIcon(weather.currentWeatherCode),
                contentDescription = stringResource(R.string.weather_title),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (locationName.isNotEmpty()) stringResource(R.string.weather_title_with_location, locationName) else stringResource(R.string.weather_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                // 現在の気温と天気
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = getWeatherIcon(weather.currentWeatherCode),
                        contentDescription = weather.currentWeatherCode.description,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${weather.currentTemperature.toInt()}°C",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Text(
                            text = weather.currentWeatherCode.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 週間予報
                Text(
                    text = "週間予報",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    weather.dailyForecasts.forEach { forecast ->
                        DailyForecastCard(forecast = forecast)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyForecastCard(forecast: DailyForecast) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isToday = forecast.date == today

    val dayText = when (today.daysUntil(forecast.date)) {
        0 -> "今日"
        1 -> "明日"
        else -> "${forecast.date.monthNumber}/${forecast.date.dayOfMonth}"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isToday) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
            .padding(8.dp),
    ) {
        Text(
            text = dayText,
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(modifier = Modifier.height(4.dp))
        Icon(
            imageVector = getWeatherIcon(forecast.weatherCode),
            contentDescription = forecast.weatherCode.description,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${forecast.temperatureMax.toInt()}°",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = "${forecast.temperatureMin.toInt()}°",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (forecast.precipitationSum > 0) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = stringResource(R.string.weather_precipitation),
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = "${forecast.precipitationSum.toInt()}mm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun getWeatherIcon(weatherCode: WeatherCode) = when (weatherCode) {
    WeatherCode.CLEAR_SKY,
    WeatherCode.MAINLY_CLEAR -> Icons.Default.WbSunny

    WeatherCode.PARTLY_CLOUDY,
    WeatherCode.OVERCAST,
    WeatherCode.FOG,
    WeatherCode.DEPOSITING_RIME_FOG -> Icons.Default.Cloud

    WeatherCode.DRIZZLE_LIGHT,
    WeatherCode.DRIZZLE_MODERATE,
    WeatherCode.DRIZZLE_DENSE,
    WeatherCode.FREEZING_DRIZZLE_LIGHT,
    WeatherCode.FREEZING_DRIZZLE_DENSE,
    WeatherCode.RAIN_SLIGHT,
    WeatherCode.RAIN_MODERATE,
    WeatherCode.RAIN_HEAVY,
    WeatherCode.FREEZING_RAIN_LIGHT,
    WeatherCode.FREEZING_RAIN_HEAVY,
    WeatherCode.RAIN_SHOWERS_SLIGHT,
    WeatherCode.RAIN_SHOWERS_MODERATE,
    WeatherCode.RAIN_SHOWERS_VIOLENT -> Icons.Default.WaterDrop

    WeatherCode.SNOW_SLIGHT,
    WeatherCode.SNOW_MODERATE,
    WeatherCode.SNOW_HEAVY,
    WeatherCode.SNOW_GRAINS,
    WeatherCode.SNOW_SHOWERS_SLIGHT,
    WeatherCode.SNOW_SHOWERS_HEAVY -> Icons.Default.Cloud

    WeatherCode.THUNDERSTORM,
    WeatherCode.THUNDERSTORM_WITH_SLIGHT_HAIL,
    WeatherCode.THUNDERSTORM_WITH_HEAVY_HAIL -> Icons.Default.Cloud

    WeatherCode.UNKNOWN -> Icons.Default.Cloud
}
