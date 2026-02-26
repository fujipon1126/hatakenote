package com.example.hatakenote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.hatakenote.feature.assistant.navigation.assistantScreen
import com.example.hatakenote.feature.assistant.navigation.navigateToAssistant
import com.example.hatakenote.feature.calendar.navigation.calendarScreen
import com.example.hatakenote.feature.calendar.navigation.navigateToCalendar
import com.example.hatakenote.feature.crop.navigation.cropListScreen
import com.example.hatakenote.feature.home.navigation.HomeRoute
import com.example.hatakenote.feature.home.navigation.homeScreen
import com.example.hatakenote.feature.planting.navigation.navigateToPlanting
import com.example.hatakenote.feature.planting.navigation.plantingScreen
import com.example.hatakenote.feature.plot.navigation.navigateToPlotDetail
import com.example.hatakenote.feature.plot.navigation.plotDetailScreen
import com.example.hatakenote.feature.settings.navigation.navigateToSettings
import com.example.hatakenote.feature.settings.navigation.settingsScreen
import com.example.hatakenote.feature.worklog.navigation.navigateToWorkLog
import com.example.hatakenote.feature.worklog.navigation.workLogScreen

@Composable
fun HatakeNoteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        homeScreen(
            onPlotClick = { plotId -> navController.navigateToPlotDetail(plotId) },
            onCalendarClick = { navController.navigateToCalendar() },
            onAssistantClick = { navController.navigateToAssistant() },
            onSettingsClick = { navController.navigateToSettings() },
        )

        plotDetailScreen(
            onBackClick = { navController.popBackStack() },
            onAddPlantingClick = { plotId -> navController.navigateToPlanting(initialPlotId = plotId) },
            onPlantingClick = { plantingId -> navController.navigateToPlanting(plantingId = plantingId) },
            onWorkLogClick = { plantingId, plotId ->
                navController.navigateToWorkLog(plantingId = plantingId, plotId = plotId)
            },
        )

        plantingScreen(
            onBackClick = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )

        workLogScreen(
            onBackClick = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
        )

        cropListScreen(
            onBackClick = { navController.popBackStack() },
            onCropClick = { },
        )

        calendarScreen(
            onBackClick = { navController.popBackStack() },
            onDateClick = { },
        )

        assistantScreen(
            onBackClick = { navController.popBackStack() },
        )

        settingsScreen(
            onBackClick = { navController.popBackStack() },
        )
    }
}
