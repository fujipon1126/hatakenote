package com.example.hatakenote.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hatakenote.feature.assistant.navigation.AssistantRoute
import com.example.hatakenote.feature.calendar.navigation.CalendarRoute
import com.example.hatakenote.feature.home.navigation.HomeRoute
import com.example.hatakenote.feature.settings.navigation.SettingsRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
) {
    HOME(
        route = HomeRoute,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "ホーム",
    ),
    CALENDAR(
        route = CalendarRoute,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        label = "カレンダー",
    ),
    ASSISTANT(
        route = AssistantRoute,
        selectedIcon = Icons.AutoMirrored.Filled.Chat,
        unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
        label = "チャット",
    ),
    SETTINGS(
        route = SettingsRoute,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "設定",
    ),
    ;

    companion object {
        fun fromRoute(route: Any?): TopLevelDestination? {
            return entries.find { it.route::class == route }
        }
    }
}
