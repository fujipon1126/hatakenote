package com.example.hatakenote.feature.calendar.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.calendar.CalendarRoute
import kotlinx.serialization.Serializable

@Serializable
object CalendarRoute

fun NavController.navigateToCalendar(navOptions: NavOptions? = null) {
    navigate(CalendarRoute, navOptions)
}

fun NavGraphBuilder.calendarScreen(
    onAddWorkLogClick: (String) -> Unit,
    onAddPlotPhotoClick: (String) -> Unit,
) {
    composable<CalendarRoute> {
        CalendarRoute(
            onAddWorkLogClick = onAddWorkLogClick,
            onAddPlotPhotoClick = onAddPlotPhotoClick,
        )
    }
}
