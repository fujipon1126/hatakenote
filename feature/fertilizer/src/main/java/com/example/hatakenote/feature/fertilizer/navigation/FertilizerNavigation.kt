package com.example.hatakenote.feature.fertilizer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.fertilizer.FertilizerListRoute
import kotlinx.serialization.Serializable

@Serializable
object FertilizerListRoute

fun NavController.navigateToFertilizerList(navOptions: NavOptions? = null) {
    navigate(FertilizerListRoute, navOptions)
}

fun NavGraphBuilder.fertilizerListScreen(
    onBackClick: () -> Unit,
) {
    composable<FertilizerListRoute> {
        FertilizerListRoute(
            onBackClick = onBackClick,
        )
    }
}
