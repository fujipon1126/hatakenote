package com.example.hatakenote.feature.plot.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.plot.PlotDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class PlotDetailRoute(val plotId: Long)

fun NavController.navigateToPlotDetail(plotId: Long, navOptions: NavOptions? = null) {
    navigate(PlotDetailRoute(plotId), navOptions)
}

fun NavGraphBuilder.plotDetailScreen(
    onBackClick: () -> Unit,
    onAddPlantingClick: (Long) -> Unit,
    onPlantingClick: (Long) -> Unit,
    onWorkLogClick: (Long?, Long?) -> Unit,
) {
    composable<PlotDetailRoute> {
        PlotDetailRoute(
            onBackClick = onBackClick,
            onAddPlantingClick = onAddPlantingClick,
            onPlantingClick = onPlantingClick,
            onWorkLogClick = onWorkLogClick,
        )
    }
}
