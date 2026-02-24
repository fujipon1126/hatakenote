package com.example.hatakenote.feature.crop.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.crop.CropListRoute
import kotlinx.serialization.Serializable

@Serializable
object CropListRoute

fun NavController.navigateToCropList(navOptions: NavOptions? = null) {
    navigate(CropListRoute, navOptions)
}

fun NavGraphBuilder.cropListScreen(
    onBackClick: () -> Unit,
    onCropClick: (Long) -> Unit,
) {
    composable<CropListRoute> {
        CropListRoute(
            onBackClick = onBackClick,
            onCropClick = onCropClick,
        )
    }
}
