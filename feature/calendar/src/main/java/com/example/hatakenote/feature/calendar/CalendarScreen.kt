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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.WorkType
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.todayIn

@Composable
internal fun CalendarRoute(
    onBackClick: () -> Unit,
    onAddWorkLogClick: (String) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPreviousMonth = viewModel::goToPreviousMonth,
        onNextMonth = viewModel::goToNextMonth,
        onToday = viewModel::goToToday,
        onDateSelected = viewModel::selectDate,
        onDismissBottomSheet = viewModel::clearSelectedDate,
        onAddWorkLogClick = onAddWorkLogClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarScreen(
    uiState: CalendarUiState,
    onBackClick: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDismissBottomSheet: () -> Unit,
    onAddWorkLogClick: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("カレンダー") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                },
                actions = {
                    IconButton(onClick = onToday) {
                        Icon(Icons.Default.Today, "今日")
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
            Icon(Icons.Default.ChevronLeft, "前月")
        }

        Text(
            text = "${year}年${month}月",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, "次月")
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    val dayNames = listOf("日", "月", "火", "水", "木", "金", "土")
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
        is CalendarEvent.PlantingEvent -> {
            try {
                Color(android.graphics.Color.parseColor(event.cropColor))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }
        }
        is CalendarEvent.HarvestEvent -> {
            try {
                Color(android.graphics.Color.parseColor(event.cropColor))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.tertiary
            }
        }
        is CalendarEvent.ReminderEvent -> MaterialTheme.colorScheme.error
        is CalendarEvent.WorkLogEvent -> MaterialTheme.colorScheme.secondary
    }
}

@Composable
private fun EventBottomSheetContent(
    date: LocalDate,
    events: List<CalendarEvent>,
    onAddWorkLogClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${date.year}年${date.monthNumber}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            FilledTonalButton(onClick = onAddWorkLogClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("作業を追加")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (events.isEmpty()) {
            Text(
                text = "予定はありません",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
    val (icon, title, subtitle, color) = when (event) {
        is CalendarEvent.PlantingEvent -> {
            val cropColor = try {
                Color(android.graphics.Color.parseColor(event.cropColor))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }
            EventCardData(
                icon = Icons.Default.Agriculture,
                title = "植付け: ${event.cropName}",
                subtitle = null,
                color = cropColor,
            )
        }
        is CalendarEvent.HarvestEvent -> {
            val cropColor = try {
                Color(android.graphics.Color.parseColor(event.cropColor))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.tertiary
            }
            EventCardData(
                icon = Icons.Default.Agriculture,
                title = "収穫: ${event.cropName}",
                subtitle = null,
                color = cropColor,
            )
        }
        is CalendarEvent.ReminderEvent -> {
            val statusText = if (event.reminder.isCompleted) "（完了）" else ""
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
                contentDescription = null,
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

private fun getWorkTypeName(workType: WorkType): String {
    return when (workType) {
        WorkType.FERTILIZE -> "追肥"
        WorkType.TILL -> "耕起"
        WorkType.BASE_FERTILIZE -> "元肥"
        WorkType.OTHER -> "その他作業"
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
