package com.example.hatakenote.feature.farm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.farm.FarmSelectRoute
import kotlinx.serialization.Serializable

@Serializable
object FarmSelectRoute

fun NavController.navigateToFarmSelect() {
    navigate(FarmSelectRoute) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavGraphBuilder.farmSelectScreen(
    onFarmSelected: () -> Unit,
) {
    composable<FarmSelectRoute> {
        FarmSelectRoute(
            onFarmSelected = onFarmSelected,
        )
    }
}
