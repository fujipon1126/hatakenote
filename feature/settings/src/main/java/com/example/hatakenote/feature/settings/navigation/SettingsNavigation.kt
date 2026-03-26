package com.example.hatakenote.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    navigate(SettingsRoute, navOptions)
}

fun NavGraphBuilder.settingsScreen(
    onCropListClick: () -> Unit,
    onFertilizerListClick: () -> Unit,
) {
    composable<SettingsRoute> {
        SettingsRoute(
            onCropListClick = onCropListClick,
            onFertilizerListClick = onFertilizerListClick,
        )
    }
}
