package com.example.hatakenote.feature.planting

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.Plot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import java.io.File
import java.io.FileOutputStream

@Composable
internal fun PlantingRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: PlantingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.saveSuccess, uiState.harvestSuccess) {
        if (uiState.saveSuccess || uiState.harvestSuccess) {
            onSaved()
        }
    }

    PlantingScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onCropSelected = viewModel::selectCrop,
        onPlotToggle = viewModel::togglePlotSelection,
        onDateSelected = viewModel::setPlantedDate,
        onNoteChanged = viewModel::setNote,
        onPhotoAdded = viewModel::addPhotoUri,
        onPhotoRemoved = viewModel::removePhotoUri,
        onExistingPhotoRemoved = viewModel::removeExistingPhoto,
        onShowCropSelector = viewModel::showCropSelector,
        onDismissCropSelector = viewModel::dismissCropSelector,
        onShowPlotSelector = viewModel::showPlotSelector,
        onDismissPlotSelector = viewModel::dismissPlotSelector,
        onShowDatePicker = viewModel::showDatePicker,
        onDismissDatePicker = viewModel::dismissDatePicker,
        onShowHarvestDialog = viewModel::showHarvestDialog,
        onDismissHarvestDialog = viewModel::dismissHarvestDialog,
        onHarvest = viewModel::harvest,
        onClearError = viewModel::clearError,
        onSave = {
            viewModel.save { uri, plantingId ->
                withContext(Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val fileName = "planting_${plantingId}_${System.currentTimeMillis()}.jpg"
                        val file = File(context.filesDir, fileName)
                        inputStream?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                        file.absolutePath
                    } catch (e: Exception) {
                        null
                    }
                }
            }
        },
        canSave = viewModel.canSave(),
        canHarvest = viewModel.canHarvest(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlantingScreen(
    uiState: PlantingUiState,
    onBackClick: () -> Unit,
    onCropSelected: (Crop) -> Unit,
    onPlotToggle: (Long) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onNoteChanged: (String) -> Unit,
    onPhotoAdded: (Uri) -> Unit,
    onPhotoRemoved: (Uri) -> Unit,
    onExistingPhotoRemoved: (com.example.hatakenote.core.domain.model.PlantingPhoto) -> Unit,
    onShowCropSelector: () -> Unit,
    onDismissCropSelector: () -> Unit,
    onShowPlotSelector: () -> Unit,
    onDismissPlotSelector: () -> Unit,
    onShowDatePicker: () -> Unit,
    onDismissDatePicker: () -> Unit,
    onShowHarvestDialog: () -> Unit,
    onDismissHarvestDialog: () -> Unit,
    onHarvest: (LocalDate) -> Unit,
    onClearError: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    canHarvest: Boolean,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        uris.forEach { uri ->
            onPhotoAdded(uri)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "作付け編集" else "作付け登録") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "戻る")
                    }
                },
                actions = {
                    if (canHarvest) {
                        TextButton(onClick = onShowHarvestDialog) {
                            Text("収穫", color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    TextButton(
                        onClick = onSave,
                        enabled = canSave,
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text("保存")
                        }
                    }
                }
            )
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Crop Selection
                item {
                    SectionCard(title = "作物") {
                        CropSelector(
                            selectedCrop = uiState.selectedCrop,
                            onClick = onShowCropSelector,
                        )
                    }
                }

                // Plot Selection
                item {
                    SectionCard(title = "区画（複数選択可）") {
                        PlotSelector(
                            plots = uiState.plots,
                            selectedPlotIds = uiState.selectedPlotIds,
                            onPlotToggle = onPlotToggle,
                        )
                    }
                }

                // Date Selection
                item {
                    SectionCard(title = "植付日") {
                        DateSelector(
                            date = uiState.plantedDate,
                            onClick = onShowDatePicker,
                        )
                    }
                }

                // Note
                item {
                    SectionCard(title = "メモ") {
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = onNoteChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("メモを入力...") },
                            minLines = 2,
                            maxLines = 4,
                        )
                    }
                }

                // Photos
                item {
                    SectionCard(title = "写真") {
                        PhotoSection(
                            existingPhotos = uiState.photos,
                            pendingPhotoUris = uiState.pendingPhotoUris,
                            onAddPhoto = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onRemovePendingPhoto = onPhotoRemoved,
                            onRemoveExistingPhoto = onExistingPhotoRemoved,
                        )
                    }
                }
            }
        }

        // Crop Selector Dialog
        if (uiState.showCropSelector) {
            CropSelectorDialog(
                crops = uiState.crops,
                selectedCrop = uiState.selectedCrop,
                onCropSelected = onCropSelected,
                onDismiss = onDismissCropSelector,
            )
        }

        // Date Picker Dialog
        if (uiState.showDatePicker) {
            PlantingDatePickerDialog(
                initialDate = uiState.plantedDate,
                onDateSelected = onDateSelected,
                onDismiss = onDismissDatePicker,
            )
        }

        // Harvest Dialog
        if (uiState.showHarvestDialog) {
            HarvestDialog(
                onConfirm = onHarvest,
                onDismiss = onDismissHarvestDialog,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        content()
    }
}

@Composable
private fun CropSelector(
    selectedCrop: Crop?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedCrop != null) {
                try {
                    Color(android.graphics.Color.parseColor(selectedCrop.colorHex)).copy(alpha = 0.1f)
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectedCrop != null) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(selectedCrop.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }
                        ),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedCrop.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = "作物を選択してください",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlotSelector(
    plots: List<Plot>,
    selectedPlotIds: Set<Long>,
    onPlotToggle: (Long) -> Unit,
) {
    if (plots.isEmpty()) {
        Text(
            text = "区画がありません。先に区画を作成してください。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            plots.forEach { plot ->
                val isSelected = selectedPlotIds.contains(plot.id)
                FilterChip(
                    selected = isSelected,
                    onClick = { onPlotToggle(plot.id) },
                    label = { Text(plot.name) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DateSelector(
    date: LocalDate,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${date.year}年${date.monthNumber}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun PhotoSection(
    existingPhotos: List<com.example.hatakenote.core.domain.model.PlantingPhoto>,
    pendingPhotoUris: List<Uri>,
    onAddPhoto: () -> Unit,
    onRemovePendingPhoto: (Uri) -> Unit,
    onRemoveExistingPhoto: (com.example.hatakenote.core.domain.model.PlantingPhoto) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            // Add Photo Button
            Card(
                modifier = Modifier
                    .size(100.dp)
                    .clickable(onClick = onAddPhoto),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "写真を追加",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "追加",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Existing Photos
        items(existingPhotos) { photo ->
            PhotoItem(
                uri = Uri.parse(photo.filePath),
                onRemove = { onRemoveExistingPhoto(photo) },
            )
        }

        // Pending Photos
        items(pendingPhotoUris) { uri ->
            PhotoItem(
                uri = uri,
                onRemove = { onRemovePendingPhoto(uri) },
            )
        }
    }
}

@Composable
private fun PhotoItem(
    uri: Uri,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier.size(100.dp),
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "写真",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "削除",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onError,
            )
        }
    }
}

@Composable
private fun CropSelectorDialog(
    crops: List<Crop>,
    selectedCrop: Crop?,
    onCropSelected: (Crop) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("作物を選択") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(crops) { crop ->
                    CropItem(
                        crop = crop,
                        isSelected = crop.id == selectedCrop?.id,
                        onClick = { onCropSelected(crop) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    )
}

@Composable
private fun CropItem(
    crop: Crop,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val cropColor = try {
        Color(android.graphics.Color.parseColor(crop.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                cropColor.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(cropColor),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = crop.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantingDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        onDateSelected(localDate)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HarvestDialog(
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                        onConfirm(localDate)
                    }
                }
            ) {
                Text("収穫を記録")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
    ) {
        Column {
            Text(
                text = "収穫日を選択",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            DatePicker(state = datePickerState)
        }
    }
}
