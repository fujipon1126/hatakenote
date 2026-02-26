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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.DailyForecast
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotWithCurrentPlanting
import com.example.hatakenote.core.domain.model.Reminder
import com.example.hatakenote.core.domain.model.Weather
import com.example.hatakenote.core.domain.model.WeatherCode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

@Composable
internal fun HomeRoute(
    onPlotClick: (Long) -> Unit,
    onCalendarClick: () -> Unit,
    onAssistantClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onPlotClick = onPlotClick,
        onCalendarClick = onCalendarClick,
        onAssistantClick = onAssistantClick,
        onSettingsClick = onSettingsClick,
        onAddPlotClick = viewModel::showAddPlotDialog,
        onDismissPlotDialog = viewModel::dismissPlotDialog,
        onSavePlot = viewModel::savePlot,
        onCompleteReminder = viewModel::completeReminder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: HomeUiState,
    onPlotClick: (Long) -> Unit,
    onCalendarClick: () -> Unit,
    onAssistantClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddPlotClick: () -> Unit,
    onDismissPlotDialog: () -> Unit,
    onSavePlot: (String, Int, Int, Int, Int) -> Unit,
    onCompleteReminder: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("畑ノート") },
                actions = {
                    IconButton(onClick = onCalendarClick) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "カレンダー"
                        )
                    }
                    IconButton(onClick = onAssistantClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "AIアシスタント"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "設定"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlotClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "区画を追加"
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
                    text = "畑マップ",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                if (uiState.plots.isEmpty()) {
                    EmptyPlotMessage(onAddPlotClick = onAddPlotClick)
                } else {
                    PlotGrid(
                        plots = uiState.plots,
                        gridColumns = uiState.gridColumns,
                        gridRows = uiState.gridRows,
                        onPlotClick = onPlotClick,
                    )
                }
            }
        }

        if (uiState.showAddPlotDialog) {
            AddEditPlotDialog(
                editingPlot = uiState.editingPlot,
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
            text = "区画がまだありません",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "右下の＋ボタンから区画を追加してください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PlotGrid(
    plots: List<PlotWithCurrentPlanting>,
    gridColumns: Int,
    gridRows: Int,
    onPlotClick: (Long) -> Unit,
) {
    // Create a grid layout
    val cellSize = 80.dp
    val gap = 4.dp

    // Create 2D array to track occupied cells
    val occupiedCells = Array(gridRows) { BooleanArray(gridColumns) }

    // Mark occupied cells
    plots.forEach { plotWithPlanting ->
        val plot = plotWithPlanting.plot
        for (y in plot.gridY until minOf(plot.gridY + plot.height, gridRows)) {
            for (x in plot.gridX until minOf(plot.gridX + plot.width, gridColumns)) {
                occupiedCells[y][x] = true
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        for (row in 0 until gridRows) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                for (col in 0 until gridColumns) {
                    // Find if there's a plot starting at this cell
                    val plotAtCell = plots.find { it.plot.gridX == col && it.plot.gridY == row }

                    if (plotAtCell != null) {
                        PlotCell(
                            plotWithPlanting = plotAtCell,
                            cellSize = cellSize,
                            gap = gap,
                            onClick = { onPlotClick(plotAtCell.plot.id) },
                        )
                    } else if (!occupiedCells[row][col]) {
                        // Empty cell (not occupied by any plot)
                        EmptyCell(cellSize = cellSize)
                    }
                    // Skip cells that are part of a multi-cell plot but not the starting cell
                }
            }
        }
    }
}

@Composable
private fun PlotCell(
    plotWithPlanting: PlotWithCurrentPlanting,
    cellSize: androidx.compose.ui.unit.Dp,
    gap: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    val plot = plotWithPlanting.plot
    val currentPlantings = plotWithPlanting.currentPlantings

    // Calculate cell dimensions including gaps for multi-cell plots
    val width = cellSize * plot.width + gap * (plot.width - 1)
    val height = cellSize * plot.height + gap * (plot.height - 1)

    // Determine background color based on planted crops
    val backgroundColor = if (currentPlantings.isNotEmpty()) {
        val colorHex = currentPlantings.first().crop.colorHex
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primaryContainer
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier
            .width(width)
            .height(height)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = plot.name,
                style = MaterialTheme.typography.labelMedium,
                color = if (currentPlantings.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (currentPlantings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                currentPlantings.take(2).forEach { plantingWithCrop ->
                    Text(
                        text = plantingWithCrop.crop.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (currentPlantings.size > 2) {
                    Text(
                        text = "+${currentPlantings.size - 2}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                        color = Color.White.copy(alpha = 0.7f),
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
private fun EmptyCell(cellSize: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(cellSize)
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
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Int, Int) -> Unit,
) {
    var name by remember { mutableStateOf(editingPlot?.name ?: "") }
    var gridX by remember { mutableStateOf(editingPlot?.gridX?.toString() ?: "0") }
    var gridY by remember { mutableStateOf(editingPlot?.gridY?.toString() ?: "0") }
    var width by remember { mutableStateOf(editingPlot?.width?.toString() ?: "1") }
    var height by remember { mutableStateOf(editingPlot?.height?.toString() ?: "1") }

    val isEdit = editingPlot != null
    val title = if (isEdit) "区画を編集" else "区画を追加"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("区画名") },
                    placeholder = { Text("例: A-1") },
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "今週のリマインダー",
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
                    contentDescription = "完了",
                    tint = MaterialTheme.colorScheme.primary,
                )
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
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (locationName.isNotEmpty()) "${locationName}の天気" else "今日の天気",
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
                        contentDescription = null,
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
            contentDescription = null,
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
                    contentDescription = null,
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
