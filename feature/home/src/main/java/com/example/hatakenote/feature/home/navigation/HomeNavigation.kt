package com.example.hatakenote.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.home.HomeRoute
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeRoute, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onPlotClick: (Long) -> Unit,
    onCalendarClick: () -> Unit,
    onAssistantClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    composable<HomeRoute> {
        HomeRoute(
            onPlotClick = onPlotClick,
            onCalendarClick = onCalendarClick,
            onAssistantClick = onAssistantClick,
            onSettingsClick = onSettingsClick,
        )
    }
}
