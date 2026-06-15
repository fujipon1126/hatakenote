package com.example.hatakenote.feature.crop

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hatakenote.core.ui.util.parseColorSafe
import com.example.hatakenote.feature.crop.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.CropFamily

@Composable
internal fun CropListRoute(
    onBackClick: () -> Unit,
    onCropClick: (Long) -> Unit,
    viewModel: CropListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CropListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCropClick = onCropClick,
        onAddClick = viewModel::showAddDialog,
        onEditClick = viewModel::showEditDialog,
        onDeleteClick = viewModel::showDeleteConfirmDialog,
        onToggleActive = viewModel::toggleCropActive,
        onDismissEditDialog = viewModel::dismissEditDialog,
        onSaveCrop = viewModel::saveCrop,
        onDismissDeleteDialog = viewModel::dismissDeleteConfirmDialog,
        onConfirmDelete = viewModel::deleteCrop,
        onClearError = viewModel::clearError,
        onClearSaveSuccess = viewModel::clearSaveSuccess,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CropListScreen(
    uiState: CropListUiState,
    onBackClick: () -> Unit,
    onCropClick: (Long) -> Unit,
    onAddClick: () -> Unit,
    onEditClick: (Crop) -> Unit,
    onDeleteClick: (Crop) -> Unit,
    onToggleActive: (Crop) -> Unit,
    onDismissEditDialog: () -> Unit,
    onSaveCrop: (String, Long, String) -> Unit,
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

    val savedMessage = stringResource(R.string.crop_saved)
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar(savedMessage)
            onClearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crop_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.crop_back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, stringResource(R.string.crop_list_add))
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
        } else if (uiState.groupedCrops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Grass,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.crop_list_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.crop_list_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            val unknownFamily = stringResource(R.string.crop_family_unknown)
            var expandedFamilyIds by rememberSaveable { mutableStateOf(emptyList<Long>()) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.groupedCrops.forEach { group ->
                    val familyKey = group.family?.id ?: ORPHAN_FAMILY_KEY
                    val isExpanded = familyKey in expandedFamilyIds
                    val familyName = group.family?.name ?: unknownFamily
                    item(key = "header-$familyKey") {
                        CropFamilyHeader(
                            familyName = familyName,
                            count = group.crops.size,
                            isExpanded = isExpanded,
                            onToggle = {
                                expandedFamilyIds = if (isExpanded) {
                                    expandedFamilyIds - familyKey
                                } else {
                                    expandedFamilyIds + familyKey
                                }
                            },
                        )
                    }
                    if (isExpanded) {
                        items(group.crops, key = { "crop-${it.id}" }) { crop ->
                            CropListItem(
                                crop = crop,
                                onClick = { onCropClick(crop.id) },
                                onEditClick = { onEditClick(crop) },
                                onDeleteClick = { onDeleteClick(crop) },
                                onToggleActive = { onToggleActive(crop) },
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit/Add Dialog
    if (uiState.showEditDialog) {
        CropEditDialog(
            crop = uiState.editingCrop,
            families = uiState.families,
            onDismiss = onDismissEditDialog,
            onSave = onSaveCrop,
        )
    }

    // Delete Confirmation Dialog
    if (uiState.showDeleteConfirmDialog && uiState.deletingCrop != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text(stringResource(R.string.crop_list_delete_title)) },
            text = {
                Text(stringResource(R.string.crop_list_delete_message, uiState.deletingCrop.name))
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                ) {
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

private const val ORPHAN_FAMILY_KEY = -1L

@Composable
private fun CropFamilyHeader(
    familyName: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = familyName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CropListItem(
    crop: Crop,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleActive: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (crop.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(parseColorSafe(crop.colorHex)),
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Crop info
            Text(
                text = crop.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (crop.isActive) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f),
            )

            // Active switch
            Switch(
                checked = crop.isActive,
                onCheckedChange = { onToggleActive() },
            )

            // Edit button
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.crop_edit),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            // Delete button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.crop_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CropEditDialog(
    crop: Crop?,
    families: List<CropFamily>,
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit,
) {
    val isEdit = crop != null
    var name by remember { mutableStateOf(crop?.name ?: "") }
    var selectedFamilyId by remember { mutableStateOf(crop?.familyId ?: families.firstOrNull()?.id ?: 0L) }
    var colorHex by remember { mutableStateOf(crop?.colorHex ?: "#4CAF50") }
    var familyDropdownExpanded by remember { mutableStateOf(false) }
    var colorDropdownExpanded by remember { mutableStateOf(false) }

    val selectedFamily = families.find { it.id == selectedFamilyId }

    // Predefined colors
    val colors = listOf(
        "#4CAF50" to "緑",
        "#8BC34A" to "ライトグリーン",
        "#CDDC39" to "ライム",
        "#FFEB3B" to "黄色",
        "#FFC107" to "アンバー",
        "#FF9800" to "オレンジ",
        "#FF5722" to "ディープオレンジ",
        "#F44336" to "赤",
        "#E91E63" to "ピンク",
        "#9C27B0" to "紫",
        "#673AB7" to "ディープパープル",
        "#3F51B5" to "インディゴ",
        "#2196F3" to "青",
        "#03A9F4" to "ライトブルー",
        "#00BCD4" to "シアン",
        "#009688" to "ティール",
        "#795548" to "茶色",
        "#607D8B" to "ブルーグレー",
    )

    val dialogTitle = if (isEdit) {
        stringResource(R.string.crop_list_edit)
    } else {
        stringResource(R.string.crop_list_add)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.crop_name)) },
                    placeholder = { Text(stringResource(R.string.crop_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Family dropdown
                ExposedDropdownMenuBox(
                    expanded = familyDropdownExpanded,
                    onExpandedChange = { familyDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedFamily?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.crop_family)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = familyDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = familyDropdownExpanded,
                        onDismissRequest = { familyDropdownExpanded = false },
                    ) {
                        families.forEach { family ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(family.name)
                                        Text(
                                            text = stringResource(R.string.crop_rotation_years, family.rotationYears),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                                onClick = {
                                    selectedFamilyId = family.id
                                    familyDropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                // Color dropdown
                ExposedDropdownMenuBox(
                    expanded = colorDropdownExpanded,
                    onExpandedChange = { colorDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = colors.find { it.first == colorHex }?.second ?: colorHex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.crop_color)) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseColorSafe(colorHex)),
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = colorDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = colorDropdownExpanded,
                        onDismissRequest = { colorDropdownExpanded = false },
                    ) {
                        colors.forEach { (hex, label) ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(parseColorSafe(hex)),
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(label)
                                    }
                                },
                                onClick = {
                                    colorHex = hex
                                    colorDropdownExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, selectedFamilyId, colorHex) },
                enabled = name.isNotBlank() && selectedFamilyId > 0,
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
