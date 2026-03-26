package com.example.hatakenote.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.hatakenote.feature.assistant.navigation.assistantScreen
import com.example.hatakenote.feature.auth.navigation.LoginRoute
import com.example.hatakenote.feature.auth.navigation.loginScreen
import com.example.hatakenote.feature.calendar.navigation.calendarScreen
import com.example.hatakenote.feature.farm.navigation.FarmSelectRoute
import com.example.hatakenote.feature.farm.navigation.farmSelectScreen
import com.example.hatakenote.feature.crop.navigation.cropDetailScreen
import com.example.hatakenote.feature.crop.navigation.cropListScreen
import com.example.hatakenote.feature.crop.navigation.navigateToCropDetail
import com.example.hatakenote.feature.crop.navigation.navigateToCropList
import com.example.hatakenote.feature.fertilizer.navigation.fertilizerListScreen
import com.example.hatakenote.feature.fertilizer.navigation.navigateToFertilizerList
import com.example.hatakenote.feature.home.navigation.HomeRoute
import com.example.hatakenote.feature.home.navigation.homeScreen
import com.example.hatakenote.feature.planting.navigation.navigateToPlanting
import com.example.hatakenote.feature.planting.navigation.plantingScreen
import com.example.hatakenote.feature.plot.navigation.navigateToPlotDetail
import com.example.hatakenote.feature.plot.navigation.plotDetailScreen
import com.example.hatakenote.feature.settings.navigation.settingsScreen
import com.example.hatakenote.feature.worklog.navigation.navigateToWorkLog
import com.example.hatakenote.feature.worklog.navigation.workLogScreen

@Composable
fun HatakeNoteNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Any = HomeRoute,
    webClientId: String = "",
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        loginScreen(
            onLoginSuccess = {
                navController.navigate(FarmSelectRoute) {
                    popUpTo(LoginRoute) { inclusive = true }
                }
            },
            webClientId = webClientId,
        )

        farmSelectScreen(
            onFarmSelected = {
                navController.navigate(HomeRoute) {
                    popUpTo(FarmSelectRoute) { inclusive = true }
                }
            },
        )

        homeScreen(
            onPlotClick = { plotId -> navController.navigateToPlotDetail(plotId) },
            onNavigateToFarmSelect = {
                navController.navigate(FarmSelectRoute)
            },
        )

        plotDetailScreen(
            onBackClick = { navController.popBackStack() },
            onAddPlantingClick = { plotId -> navController.navigateToPlanting(initialPlotId = plotId) },
            onPlantingClick = { plantingId -> navController.navigateToPlanting(plantingId = plantingId) },
            onWorkLogClick = { plantingId, plotId ->
                navController.navigateToWorkLog(plantingId = plantingId, plotId = plotId)
            },
            onWorkLogEditClick = { workLogId, plotId ->
                navController.navigateToWorkLog(workLogId = workLogId, plotId = plotId)
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
            onCropClick = { cropId -> navController.navigateToCropDetail(cropId) },
        )

        cropDetailScreen(
            onBackClick = { navController.popBackStack() },
            onPlantingClick = { plantingId -> navController.navigateToPlanting(plantingId = plantingId) },
        )

        calendarScreen(
            onAddWorkLogClick = { dateString ->
                navController.navigateToWorkLog(workDate = dateString)
            },
        )

        assistantScreen()

        fertilizerListScreen(
            onBackClick = { navController.popBackStack() },
        )

        settingsScreen(
            onCropListClick = { navController.navigateToCropList() },
            onFertilizerListClick = { navController.navigateToFertilizerList() },
        )
    }
}
