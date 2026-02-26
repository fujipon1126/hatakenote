package com.example.hatakenote.feature.worklog.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.hatakenote.feature.worklog.WorkLogRoute
import kotlinx.serialization.Serializable

@Serializable
data class WorkLogRoute(
    val workLogId: Long? = null,
    val plantingId: Long? = null,
    val plotId: Long? = null,
    val workDate: String? = null,
)

fun NavController.navigateToWorkLog(
    workLogId: Long? = null,
    plantingId: Long? = null,
    plotId: Long? = null,
    workDate: String? = null,
    navOptions: NavOptions? = null,
) {
    navigate(WorkLogRoute(workLogId, plantingId, plotId, workDate), navOptions)
}

fun NavGraphBuilder.workLogScreen(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
) {
    composable<WorkLogRoute> {
        WorkLogRoute(
            onBackClick = onBackClick,
            onSaved = onSaved,
        )
    }
}
