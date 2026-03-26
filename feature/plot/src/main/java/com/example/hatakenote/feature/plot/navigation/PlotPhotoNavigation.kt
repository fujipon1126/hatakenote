package com.example.hatakenote.feature.plot.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.plot.PlotPhotoRoute
import kotlinx.serialization.Serializable

@Serializable
data class PlotPhotoRoute(
    val plotId: Long? = null,
    val photoDate: String? = null,
)

fun NavController.navigateToPlotPhoto(
    plotId: Long? = null,
    photoDate: String? = null,
    navOptions: NavOptions? = null,
) {
    navigate(PlotPhotoRoute(plotId, photoDate), navOptions)
}

fun NavGraphBuilder.plotPhotoScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
) {
    composable<PlotPhotoRoute> {
        PlotPhotoRoute(
            onBackClick = onBackClick,
            onSaved = onSaved,
        )
    }
}
