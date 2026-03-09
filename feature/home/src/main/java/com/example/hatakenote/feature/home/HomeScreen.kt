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
import com.example.hatakenote.core.ui.util.contrastTextColor
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

@Composable
private fun PlotGrid(
    plots: List<PlotWithCurrentPlanting>,
    onPlotClick: (Long) -> Unit,
) {
    val gap = 8.dp

    // Group plots by side
    val leftPlots = plots
        .filter { it.plot.side == PlotSide.LEFT }
        .sortedBy { it.plot.number }
    val rightPlots = plots
        .filter { it.plot.side == PlotSide.RIGHT }
        .sortedBy { it.plot.number }

    val maxNumber = maxOf(
        leftPlots.maxOfOrNull { it.plot.number } ?: 0,
        rightPlots.maxOfOrNull { it.plot.number } ?: 0,
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
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

        for (number in 1..maxNumber) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Left cell
                val leftPlot = leftPlots.find { it.plot.number == number }
                if (leftPlot != null) {
                    PlotCell(
                        plotWithPlanting = leftPlot,
                        modifier = Modifier.weight(1f),
                        onClick = { onPlotClick(leftPlot.plot.id) },
                    )
                } else {
                    EmptyCell(modifier = Modifier.weight(1f))
                }

                // Right cell
                val rightPlot = rightPlots.find { it.plot.number == number }
                if (rightPlot != null) {
                    PlotCell(
                        plotWithPlanting = rightPlot,
                        modifier = Modifier.weight(1f),
                        onClick = { onPlotClick(rightPlot.plot.id) },
                    )
                } else {
                    EmptyCell(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PlotCell(
    plotWithPlanting: PlotWithCurrentPlanting,
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
            .aspectRatio(1.6f)
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
            Text(
                text = plot.name,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
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
private fun EmptyCell(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1.6f)
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
