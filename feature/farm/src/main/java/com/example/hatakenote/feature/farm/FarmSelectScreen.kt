package com.example.hatakenote.feature.farm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.Farm

@Composable
internal fun FarmSelectRoute(
    onFarmSelected: () -> Unit,
    viewModel: FarmViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.farmSelected) {
        if (uiState.farmSelected) {
            viewModel.clearFarmSelected()
            onFarmSelected()
        }
    }

    FarmSelectScreen(
        uiState = uiState,
        onFarmClick = viewModel::selectFarm,
        onCreateClick = viewModel::showCreateDialog,
        onJoinClick = viewModel::showJoinDialog,
        onInviteClick = viewModel::showInviteDialog,
        onCreateDismiss = viewModel::hideCreateDialog,
        onJoinDismiss = viewModel::hideJoinDialog,
        onInviteDismiss = viewModel::hideInviteDialog,
        onCreateConfirm = viewModel::createFarm,
        onJoinConfirm = viewModel::joinFarm,
        onRegenerateCode = viewModel::regenerateInviteCode,
        onErrorDismiss = viewModel::clearError,
        onSuccessDismiss = viewModel::clearSuccess,
    )
}

@Composable
internal fun FarmSelectScreen(
    uiState: FarmUiState,
    onFarmClick: (Farm) -> Unit,
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onInviteClick: (Farm) -> Unit,
    onCreateDismiss: () -> Unit,
    onJoinDismiss: () -> Unit,
    onInviteDismiss: () -> Unit,
    onCreateConfirm: (String) -> Unit,
    onJoinConfirm: (String) -> Unit,
    onRegenerateCode: (String) -> Unit,
    onErrorDismiss: () -> Unit,
    onSuccessDismiss: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onErrorDismiss()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onSuccessDismiss()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.farm_select_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.farm_select_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (uiState.farms.isEmpty()) {
                        EmptyFarmContent(
                            onCreateClick = onCreateClick,
                            onJoinClick = onJoinClick,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(uiState.farms) { farm ->
                                FarmCard(
                                    farm = farm,
                                    isSelected = farm.id == uiState.selectedFarmId,
                                    onClick = { onFarmClick(farm) },
                                    onInviteClick = { onInviteClick(farm) },
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                onClick = onJoinClick,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.farm_join_existing))
                            }
                            Button(
                                onClick = onCreateClick,
                                modifier = Modifier.weight(1f),
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.farm_create_new))
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Dialog
    if (uiState.showCreateDialog) {
        CreateFarmDialog(
            isLoading = uiState.isCreating,
            onDismiss = onCreateDismiss,
            onConfirm = onCreateConfirm,
        )
    }

    // Join Dialog
    if (uiState.showJoinDialog) {
        JoinFarmDialog(
            isLoading = uiState.isJoining,
            onDismiss = onJoinDismiss,
            onConfirm = onJoinConfirm,
        )
    }

    // Invite Dialog
    if (uiState.showInviteDialog && uiState.currentInviteCode != null) {
        InviteDialog(
            inviteCode = uiState.currentInviteCode,
            farmId = uiState.selectedFarmId ?: "",
            onDismiss = onInviteDismiss,
            onRegenerateCode = onRegenerateCode,
        )
    }
}

@Composable
private fun EmptyFarmContent(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Agriculture,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.farm_no_farms),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.farm_no_farms_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.farm_create_new))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Icon(Icons.Default.GroupAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.farm_join_existing))
        }
    }
}

@Composable
private fun FarmCard(
    farm: Farm,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInviteClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = farm.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.farm_members_count, farm.memberIds.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onInviteClick) {
                Icon(
                    imageVector = Icons.Default.GroupAdd,
                    contentDescription = stringResource(R.string.farm_invite_title),
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.farm_selected),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun CreateFarmDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var farmName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.farm_create_title)) },
        text = {
            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text(stringResource(R.string.farm_create_name_label)) },
                placeholder = { Text(stringResource(R.string.farm_create_name_placeholder)) },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(farmName) },
                enabled = farmName.isNotBlank() && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.farm_create_button))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.farm_cancel))
            }
        },
    )
}

@Composable
private fun JoinFarmDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var inviteCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(R.string.farm_join_title)) },
        text = {
            OutlinedTextField(
                value = inviteCode,
                onValueChange = { inviteCode = it.uppercase().take(6) },
                label = { Text(stringResource(R.string.farm_join_code_label)) },
                placeholder = { Text(stringResource(R.string.farm_join_code_placeholder)) },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(inviteCode) },
                enabled = inviteCode.length == 6 && !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.farm_join_button))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
            ) {
                Text(stringResource(R.string.farm_cancel))
            }
        },
    )
}

@Composable
private fun InviteDialog(
    inviteCode: String,
    farmId: String,
    onDismiss: () -> Unit,
    onRegenerateCode: (String) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.farm_invite_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.farm_invite_code_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = inviteCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(inviteCode))
                                copied = true
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.farm_invite_copy),
                            )
                        }
                    }
                }
                if (copied) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.farm_invite_copied),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onRegenerateCode(farmId) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.farm_invite_regenerate))
            }
        },
    )
}

