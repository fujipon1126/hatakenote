package com.example.hatakenote.feature.fertilizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.Fertilizer
import com.example.hatakenote.feature.fertilizer.R

@Composable
internal fun FertilizerListRoute(
    onBackClick: () -> Unit,
    viewModel: FertilizerListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FertilizerListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onAddClick = viewModel::showAddDialog,
        onEditClick = viewModel::showEditDialog,
        onDeleteClick = viewModel::showDeleteConfirmDialog,
        onDismissEditDialog = viewModel::dismissEditDialog,
        onSaveFertilizer = viewModel::saveFertilizer,
        onDismissDeleteDialog = viewModel::dismissDeleteConfirmDialog,
        onConfirmDelete = viewModel::deleteFertilizer,
        onClearError = viewModel::clearError,
        onClearSaveSuccess = viewModel::clearSaveSuccess,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FertilizerListScreen(
    uiState: FertilizerListUiState,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Fertilizer) -> Unit,
    onDeleteClick: (Fertilizer) -> Unit,
    onDismissEditDialog: () -> Unit,
    onSaveFertilizer: (String, String, String) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onClearError: () -> Unit,
    onClearSaveSuccess: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    val savedMessage = stringResource(R.string.fertilizer_saved)
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(savedMessage)
            onClearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fertilizer_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.fertilizer_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, stringResource(R.string.fertilizer_list_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.fertilizers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.fertilizer_list_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.fertilizer_list_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.fertilizers, key = { it.id }) { fertilizer ->
                    FertilizerListItem(
                        fertilizer = fertilizer,
                        onEditClick = { onEditClick(fertilizer) },
                        onDeleteClick = { onDeleteClick(fertilizer) },
                    )
                }
            }
        }
    }

    // Edit/Add Dialog
    if (uiState.showEditDialog) {
        FertilizerEditDialog(
            fertilizer = uiState.editingFertilizer,
            onDismiss = onDismissEditDialog,
            onSave = onSaveFertilizer,
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmDialog && uiState.deletingFertilizer != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text(stringResource(R.string.fertilizer_list_delete_title)) },
            text = {
                Text(stringResource(R.string.fertilizer_list_delete_message, uiState.deletingFertilizer.name))
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun FertilizerListItem(
    fertilizer: Fertilizer,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fertilizer.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (fertilizer.defaultAmount.isNotEmpty()) {
                    Text(
                        text = fertilizer.defaultAmount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (fertilizer.note.isNotEmpty()) {
                    Text(
                        text = fertilizer.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.fertilizer_edit),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.fertilizer_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun FertilizerEditDialog(
    fertilizer: Fertilizer?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit,
) {
    val isEdit = fertilizer != null
    var name by remember { mutableStateOf(fertilizer?.name ?: "") }
    var defaultAmount by remember { mutableStateOf(fertilizer?.defaultAmount ?: "") }
    var note by remember { mutableStateOf(fertilizer?.note ?: "") }

    val dialogTitle = if (isEdit) {
        stringResource(R.string.fertilizer_list_edit)
    } else {
        stringResource(R.string.fertilizer_list_add)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.fertilizer_name)) },
                    placeholder = { Text(stringResource(R.string.fertilizer_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = defaultAmount,
                    onValueChange = { defaultAmount = it },
                    label = { Text(stringResource(R.string.fertilizer_default_amount)) },
                    placeholder = { Text(stringResource(R.string.fertilizer_default_amount_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.fertilizer_note)) },
                    placeholder = { Text(stringResource(R.string.fertilizer_note_hint)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, defaultAmount, note) },
                enabled = name.isNotBlank(),
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
