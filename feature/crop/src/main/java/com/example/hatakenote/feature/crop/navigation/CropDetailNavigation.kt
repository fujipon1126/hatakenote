package com.example.hatakenote.feature.crop.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.crop.CropDetailRoute
import kotlinx.serialization.Serializable

@Serializable
data class CropDetailRoute(val cropId: Long)

fun NavController.navigateToCropDetail(cropId: Long, navOptions: NavOptions? = null) {
    navigate(CropDetailRoute(cropId), navOptions)
}

fun NavGraphBuilder.cropDetailScreen(
    onBackClick: () -> Unit,
    onPlantingClick: (Long) -> Unit,
) {
    composable<CropDetailRoute> {
        CropDetailRoute(
            onBackClick = onBackClick,
            onPlantingClick = onPlantingClick,
        )
    }
}
