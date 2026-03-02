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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("保存しました")
            onClearSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("作物マスター管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, "作物を追加")
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
        } else if (uiState.crops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "作物が登録されていません",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "＋ボタンから追加してください",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                items(uiState.crops, key = { it.id }) { crop ->
                    val family = uiState.families.find { it.id == crop.familyId }
                    CropListItem(
                        crop = crop,
                        familyName = family?.name ?: "不明",
                        onEditClick = { onEditClick(crop) },
                        onDeleteClick = { onDeleteClick(crop) },
                        onToggleActive = { onToggleActive(crop) },
                    )
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
            title = { Text("作物を削除") },
            text = {
                Text("「${uiState.deletingCrop.name}」を削除しますか？\n\nこの作物を使用している作付け記録がある場合、表示に影響が出る可能性があります。")
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                ) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text("キャンセル")
                }
            },
        )
    }
}

@Composable
private fun CropListItem(
    crop: Crop,
    familyName: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleActive: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .background(parseColor(crop.colorHex)),
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Crop info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (crop.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = familyName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Active switch
            Switch(
                checked = crop.isActive,
                onCheckedChange = { onToggleActive() },
            )

            // Edit button
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "編集",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            // Delete button
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "作物を編集" else "作物を追加") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("作物名") },
                    placeholder = { Text("例: トマト") },
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
                        label = { Text("科") },
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
                                            text = "連作年数: ${family.rotationYears}年",
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
                        label = { Text("表示色") },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parseColor(colorHex)),
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
                                                .background(parseColor(hex)),
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
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}

private fun parseColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
}
