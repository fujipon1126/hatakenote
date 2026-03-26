package com.example.hatakenote.feature.calendar

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hatakenote.feature.calendar.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.WorkType
import com.example.hatakenote.core.ui.util.parseColorSafe
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.todayIn

@Composable
internal fun CalendarRoute(
    onAddWorkLogClick: (String) -> Unit,
    onAddPlotPhotoClick: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(
        uiState = uiState,
        onPreviousMonth = viewModel::goToPreviousMonth,
        onNextMonth = viewModel::goToNextMonth,
        onToday = viewModel::goToToday,
        onDateSelected = viewModel::selectDate,
        onDismissBottomSheet = viewModel::clearSelectedDate,
        onAddWorkLogClick = onAddWorkLogClick,
        onAddPlotPhotoClick = onAddPlotPhotoClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarScreen(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onAddWorkLogClick: (String) -> Unit,
    onAddPlotPhotoClick: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title)) },
                actions = {
                    IconButton(onClick = onToday) {
                        Icon(Icons.Default.Today, stringResource(R.string.calendar_today))
                    }
                },
            )
        },
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
                    .padding(16.dp),
            ) {
                // Month Navigation
                MonthNavigator(
                    year = uiState.currentYear,
                    month = uiState.currentMonth,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Day of Week Header
                DayOfWeekHeader()

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                CalendarGrid(
                    year = uiState.currentYear,
                    month = uiState.currentMonth,
                    eventsByDate = uiState.eventsByDate,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = onDateSelected,
                )
            }
        }

        // Bottom Sheet for selected date events
        if (uiState.selectedDate != null) {
            ModalBottomSheet(
                onDismissRequest = onDismissBottomSheet,
                sheetState = sheetState,
            ) {
                EventBottomSheetContent(
                    date = uiState.selectedDate,
                    events = uiState.selectedDateEvents,
                    onAddWorkLogClick = {
                        onDismissBottomSheet()
                        onAddWorkLogClick(uiState.selectedDate.toString())
                    },
                    onAddPlotPhotoClick = {
                        onDismissBottomSheet()
                        onAddPlotPhotoClick(uiState.selectedDate.toString())
                    },
                )
            }
        }
    }
}

@Composable
private fun MonthNavigator(
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, stringResource(R.string.calendar_prev_month))
        }

        Text(
            text = stringResource(R.string.calendar_year_month, year, month),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, stringResource(R.string.calendar_next_month))
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    val dayNames = listOf(
        stringResource(R.string.calendar_day_sun),
        stringResource(R.string.calendar_day_mon),
        stringResource(R.string.calendar_day_tue),
        stringResource(R.string.calendar_day_wed),
        stringResource(R.string.calendar_day_thu),
        stringResource(R.string.calendar_day_fri),
        stringResource(R.string.calendar_day_sat),
    )
    val dayColors = listOf(
        MaterialTheme.colorScheme.error, // Sunday
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.primary, // Saturday
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        dayNames.forEachIndexed { index, name ->
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = dayColors[index],
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val firstDayOfMonth = LocalDate(year, month, 1)
    val daysInMonth = getDaysInMonth(year, month)

    // Calculate the day of week for the first day (0 = Sunday in our grid)
    val firstDayOfWeek = when (firstDayOfMonth.dayOfWeek) {
        DayOfWeek.SUNDAY -> 0
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
    }

    // Create list of days including empty cells for padding
    val calendarDays = mutableListOf<Int?>()

    // Add empty cells for days before the first day of month
    repeat(firstDayOfWeek) {
        calendarDays.add(null)
    }

    // Add actual days
    for (day in 1..daysInMonth) {
        calendarDays.add(day)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(calendarDays) { day ->
            if (day != null) {
                val date = LocalDate(year, month, day)
                val events = eventsByDate[date] ?: emptyList()
                val isToday = date == today
                val isSelected = date == selectedDate

                DayCell(
                    day = day,
                    date = date,
                    events = events,
                    isToday = isToday,
                    isSelected = isSelected,
                    onClick = { onDateSelected(date) },
                )
            } else {
                // Empty cell
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    date: LocalDate,
    events: List<CalendarEvent>,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val dayOfWeek = date.dayOfWeek
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        dayOfWeek == DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
        dayOfWeek == DayOfWeek.SATURDAY -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp),
                    )
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
            )

            // Event indicators
            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    events.take(3).forEach { event ->
                        val indicatorColor = getEventIndicatorColor(event)
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(indicatorColor),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getEventIndicatorColor(event: CalendarEvent): Color {
    return when (event) {
        is CalendarEvent.PlantingEvent -> parseColorSafe(event.cropColor, MaterialTheme.colorScheme.primary)
        is CalendarEvent.HarvestEvent -> parseColorSafe(event.cropColor, MaterialTheme.colorScheme.tertiary)
        is CalendarEvent.ReminderEvent -> MaterialTheme.colorScheme.error
        is CalendarEvent.WorkLogEvent -> MaterialTheme.colorScheme.secondary
        is CalendarEvent.PhotoEvent -> MaterialTheme.colorScheme.tertiary
    }
}

@Composable
private fun EventBottomSheetContent(
    date: LocalDate,
    events: List<CalendarEvent>,
    onAddWorkLogClick: () -> Unit,
    onAddPlotPhotoClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.calendar_date_format, date.year, date.monthNumber, date.dayOfMonth),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalButton(onClick = onAddWorkLogClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.calendar_add_work),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.calendar_add_work))
            }
            FilledTonalButton(onClick = onAddPlotPhotoClick) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = stringResource(R.string.calendar_add_photo),
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.calendar_add_photo))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.calendar_no_events),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.calendar_no_events_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events) { event ->
                    EventCard(event = event)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EventCard(event: CalendarEvent) {
    val completedText = stringResource(R.string.calendar_completed)
    val (icon, title, subtitle, color) = when (event) {
        is CalendarEvent.PlantingEvent -> {
            EventCardData(
                icon = Icons.Default.Agriculture,
                title = stringResource(R.string.calendar_planting, event.cropName),
                subtitle = null,
                color = parseColorSafe(event.cropColor, MaterialTheme.colorScheme.primary),
            )
        }
        is CalendarEvent.HarvestEvent -> {
            EventCardData(
                icon = Icons.Default.Agriculture,
                title = stringResource(R.string.calendar_harvest, event.cropName),
                subtitle = null,
                color = parseColorSafe(event.cropColor, MaterialTheme.colorScheme.tertiary),
            )
        }
        is CalendarEvent.ReminderEvent -> {
            val statusText = if (event.reminder.isCompleted) completedText else ""
            EventCardData(
                icon = Icons.Default.Notifications,
                title = event.reminder.title + statusText,
                subtitle = event.reminder.message,
                color = if (event.reminder.isCompleted) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
        is CalendarEvent.WorkLogEvent -> {
            val workTypeName = getWorkTypeName(event.workLog.workType)
            val title = if (event.cropName != null) {
                "$workTypeName: ${event.cropName}"
            } else {
                workTypeName
            }
            EventCardData(
                icon = Icons.Default.Build,
                title = title,
                subtitle = event.workLog.detail,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        is CalendarEvent.PhotoEvent -> {
            val title = if (event.plotName != null) {
                stringResource(R.string.calendar_photo_with_plot, event.plotName)
            } else {
                stringResource(R.string.calendar_photo)
            }
            EventCardData(
                icon = Icons.Default.PhotoCamera,
                title = title,
                subtitle = event.photo.comment,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private data class EventCardData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String?,
    val color: Color,
)

@Composable
private fun getWorkTypeName(workType: WorkType): String {
    return when (workType) {
        WorkType.FERTILIZE -> stringResource(R.string.work_type_fertilize)
        WorkType.TILL -> stringResource(R.string.work_type_till)
        WorkType.BASE_FERTILIZE -> stringResource(R.string.work_type_base_fertilize)
        WorkType.OTHER -> stringResource(R.string.work_type_other)
    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}
