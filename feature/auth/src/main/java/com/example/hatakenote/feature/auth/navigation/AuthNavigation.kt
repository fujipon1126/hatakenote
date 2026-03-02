package com.example.hatakenote.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.auth.LoginRoute
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

fun NavController.navigateToLogin() {
    navigate(LoginRoute) {
        popUpTo(0) { inclusive = true }
    }
}

fun NavGraphBuilder.loginScreen(
    onLoginSuccess: () -> Unit,
    webClientId: String,
) {
    composable<LoginRoute> {
        LoginRoute(
            onLoginSuccess = onLoginSuccess,
            webClientId = webClientId,
        )
    }
}
