package com.example.hatakenote.feature.planting.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.planting.PlantingRoute
import kotlinx.serialization.Serializable

@Serializable
data class PlantingRoute(val plantingId: Long? = null)

fun NavController.navigateToPlanting(plantingId: Long? = null, navOptions: NavOptions? = null) {
    navigate(PlantingRoute(plantingId), navOptions)
}

fun NavGraphBuilder.plantingScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
) {
    composable<PlantingRoute> {
        PlantingRoute(
            onBackClick = onBackClick,
            onSaved = onSaved,
        )
    }
}
