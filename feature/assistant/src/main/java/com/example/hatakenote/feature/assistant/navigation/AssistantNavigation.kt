package com.example.hatakenote.feature.assistant.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.assistant.AssistantRoute
import kotlinx.serialization.Serializable

@Serializable
object AssistantRoute

fun NavController.navigateToAssistant(navOptions: NavOptions? = null) {
    navigate(AssistantRoute, navOptions)
}

fun NavGraphBuilder.assistantScreen(
    onBackClick: () -> Unit,
) {
    composable<AssistantRoute> {
        AssistantRoute(onBackClick = onBackClick)
    }
}
