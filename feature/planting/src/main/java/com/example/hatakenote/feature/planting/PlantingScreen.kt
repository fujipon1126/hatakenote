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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hatakenote.feature.planting.R
import coil.compose.AsyncImage
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.Harvest
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.usecase.RotationWarning
import com.example.hatakenote.core.domain.usecase.WarningSeverity
import com.example.hatakenote.core.ui.util.parseColorSafe
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
import androidx.core.content.FileProvider
import com.example.hatakenote.core.ui.component.FullScreenPhotoViewer
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
        onPendingPhotoRemoved = viewModel::removePendingPhoto,
        onExistingPhotoRemoved = viewModel::removeExistingPhoto,
        onPhotoClick = viewModel::showPhotoDetail,
        onPendingPhotoClick = viewModel::showPendingPhotoDetail,
        onDismissPhotoDetail = viewModel::dismissPhotoDetail,
        onUpdateExistingPhoto = viewModel::updateExistingPhoto,
        onUpdatePendingPhoto = viewModel::updatePendingPhoto,
        onShowCropSelector = viewModel::showCropSelector,
        onDismissCropSelector = viewModel::dismissCropSelector,
        onShowPlotSelector = viewModel::showPlotSelector,
        onDismissPlotSelector = viewModel::dismissPlotSelector,
        onShowDatePicker = viewModel::showDatePicker,
        onDismissDatePicker = viewModel::dismissDatePicker,
        onShowHarvestDialog = viewModel::showHarvestDialog,
        onDismissHarvestDialog = viewModel::dismissHarvestDialog,
        onHarvest = { date, isFinal -> viewModel.harvest(date, isFinal) },
        onClearError = viewModel::clearError,
        onDismissRotationWarnings = viewModel::dismissRotationWarnings,
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
    onPendingPhotoRemoved: (PendingPhotoMetadata) -> Unit,
    onExistingPhotoRemoved: (com.example.hatakenote.core.domain.model.PlantingPhoto) -> Unit,
    onPhotoClick: (com.example.hatakenote.core.domain.model.PlantingPhoto) -> Unit,
    onPendingPhotoClick: (PendingPhotoMetadata) -> Unit,
    onDismissPhotoDetail: () -> Unit,
    onUpdateExistingPhoto: (com.example.hatakenote.core.domain.model.PlantingPhoto) -> Unit,
    onUpdatePendingPhoto: (PendingPhotoMetadata) -> Unit,
    onShowCropSelector: () -> Unit,
    onDismissCropSelector: () -> Unit,
    onShowPlotSelector: () -> Unit,
    onDismissPlotSelector: () -> Unit,
    onShowDatePicker: () -> Unit,
    onDismissDatePicker: () -> Unit,
    onShowHarvestDialog: () -> Unit,
    onDismissHarvestDialog: () -> Unit,
    onHarvest: (LocalDate, Boolean) -> Unit,
    onClearError: () -> Unit,
    onDismissRotationWarnings: () -> Unit,
    onSave: () -> Unit,
    canSave: Boolean,
    canHarvest: Boolean,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        uris.forEach { uri ->
            onPhotoAdded(uri)
        }
    }

    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraPhotoUri?.let { uri -> onPhotoAdded(uri) }
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
                title = { Text(if (uiState.isEditMode) stringResource(R.string.planting_edit) else stringResource(R.string.planting_add)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.planting_back))
                    }
                },
                actions = {
                    if (canHarvest) {
                        TextButton(onClick = onShowHarvestDialog) {
                            Text(stringResource(R.string.planting_harvest), color = MaterialTheme.colorScheme.tertiary)
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
                            Text(stringResource(R.string.save))
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
                    .padding(paddingValues)
                    .imePadding(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Crop Selection
                item {
                    SectionCard(title = stringResource(R.string.planting_crop)) {
                        CropSelector(
                            selectedCrop = uiState.selectedCrop,
                            onClick = onShowCropSelector,
                        )
                    }
                }

                // Plot Selection
                item {
                    val isHarvested = uiState.existingPlanting?.isActive == false
                    SectionCard(title = stringResource(R.string.planting_plot)) {
                        PlotSelector(
                            plots = uiState.plots,
                            selectedPlotIds = uiState.selectedPlotIds,
                            onPlotToggle = onPlotToggle,
                            enabled = !isHarvested,
                        )
                    }
                }

                // Rotation Warnings
                if (uiState.rotationWarnings.isNotEmpty()) {
                    item {
                        RotationWarningCard(
                            warnings = uiState.rotationWarnings,
                            onDismiss = onDismissRotationWarnings,
                        )
                    }
                }

                // Date Selection
                item {
                    SectionCard(title = stringResource(R.string.planting_date)) {
                        DateSelector(
                            date = uiState.plantedDate,
                            onClick = onShowDatePicker,
                        )
                    }
                }

                // Note
                item {
                    SectionCard(title = stringResource(R.string.planting_notes)) {
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = onNoteChanged,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.planting_notes_hint)) },
                            minLines = 2,
                            maxLines = 4,
                        )
                    }
                }

                // Photos
                item {
                    SectionCard(title = stringResource(R.string.planting_photo)) {
                        PhotoSection(
                            existingPhotos = uiState.photos,
                            pendingPhotos = uiState.pendingPhotos,
                            onAddPhoto = { showPhotoSourceDialog = true },
                            onPhotoClick = onPhotoClick,
                            onPendingPhotoClick = onPendingPhotoClick,
                        )
                    }
                }

                // Harvest History
                if (uiState.isEditMode && uiState.harvests.isNotEmpty()) {
                    item {
                        SectionCard(title = stringResource(R.string.planting_harvest_history)) {
                            HarvestHistorySection(harvests = uiState.harvests)
                        }
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
                onContinue = { date -> onHarvest(date, false) },
                onFinal = { date -> onHarvest(date, true) },
                onDismiss = onDismissHarvestDialog,
            )
        }

        // Photo Detail BottomSheet - Existing Photo
        uiState.selectedPhotoForDetail?.let { photo ->
            PhotoDetailBottomSheet(
                imageUri = Uri.parse(photo.filePath),
                initialDate = photo.takenDate,
                initialComment = photo.comment ?: "",
                onSave = { date, comment ->
                    onUpdateExistingPhoto(photo.copy(takenDate = date, comment = comment.ifBlank { null }))
                    onDismissPhotoDetail()
                },
                onDelete = { onExistingPhotoRemoved(photo) },
                onDismiss = onDismissPhotoDetail,
            )
        }

        // Photo Detail BottomSheet - Pending Photo
        uiState.selectedPendingPhotoForDetail?.let { pending ->
            PhotoDetailBottomSheet(
                imageUri = pending.uri,
                initialDate = pending.takenDate,
                initialComment = pending.comment ?: "",
                onSave = { date, comment ->
                    onUpdatePendingPhoto(pending.copy(takenDate = date, comment = comment.ifBlank { null }))
                    onDismissPhotoDetail()
                },
                onDelete = { onPendingPhotoRemoved(pending) },
                onDismiss = onDismissPhotoDetail,
            )
        }
    }

    // Photo Source Dialog
    if (showPhotoSourceDialog) {
        PhotoSourceDialog(
            onDismiss = { showPhotoSourceDialog = false },
            onCameraSelected = {
                showPhotoSourceDialog = false
                val photoFile = File(
                    context.filesDir,
                    "camera_photo_${System.currentTimeMillis()}.jpg"
                )
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile,
                )
                cameraPhotoUri = uri
                cameraLauncher.launch(uri)
            },
            onGallerySelected = {
                showPhotoSourceDialog = false
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )
    }
}

@Composable
private fun PhotoSourceDialog(
    onDismiss: () -> Unit,
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.photo_source_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onCameraSelected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.photo_source_camera),
                        modifier = Modifier.weight(1f),
                    )
                }
                TextButton(
                    onClick = onGallerySelected,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.photo_source_gallery),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
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
                parseColorSafe(selectedCrop.colorHex, MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.1f)
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
                        .background(parseColorSafe(selectedCrop.colorHex, MaterialTheme.colorScheme.primary)),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedCrop.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    text = stringResource(R.string.planting_select_crop_hint),
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
    enabled: Boolean = true,
) {
    if (plots.isEmpty()) {
        Text(
            text = stringResource(R.string.planting_no_plots),
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
                    enabled = enabled,
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = stringResource(R.string.planting_selected), modifier = Modifier.size(18.dp)) }
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
                contentDescription = stringResource(R.string.planting_select_date),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.planting_date_format, date.year, date.monthNumber, date.dayOfMonth),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private sealed interface PhotoListItem {
    val takenDate: LocalDate
    data class Existing(val photo: com.example.hatakenote.core.domain.model.PlantingPhoto) : PhotoListItem {
        override val takenDate: LocalDate get() = photo.takenDate
    }
    data class Pending(val metadata: PendingPhotoMetadata) : PhotoListItem {
        override val takenDate: LocalDate get() = metadata.takenDate
    }
}

@Composable
private fun PhotoSection(
    existingPhotos: List<com.example.hatakenote.core.domain.model.PlantingPhoto>,
    pendingPhotos: List<PendingPhotoMetadata>,
    onAddPhoto: () -> Unit,
    onPhotoClick: (com.example.hatakenote.core.domain.model.PlantingPhoto) -> Unit,
    onPendingPhotoClick: (PendingPhotoMetadata) -> Unit,
) {
    val sortedPhotos = remember(existingPhotos, pendingPhotos) {
        val existing = existingPhotos.map { PhotoListItem.Existing(it) }
        val pending = pendingPhotos.map { PhotoListItem.Pending(it) }
        (existing + pending).sortedByDescending { it.takenDate }
    }

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
                            contentDescription = stringResource(R.string.planting_add_photo),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.planting_photo_add),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        items(sortedPhotos) { item ->
            when (item) {
                is PhotoListItem.Existing -> PhotoItem(
                    uri = Uri.parse(item.photo.filePath),
                    takenDate = item.takenDate,
                    hasComment = !item.photo.comment.isNullOrBlank(),
                    onClick = { onPhotoClick(item.photo) },
                )
                is PhotoListItem.Pending -> PhotoItem(
                    uri = item.metadata.uri,
                    takenDate = item.takenDate,
                    hasComment = !item.metadata.comment.isNullOrBlank(),
                    onClick = { onPendingPhotoClick(item.metadata) },
                )
            }
        }
    }
}

@Composable
private fun PhotoItem(
    uri: Uri,
    takenDate: LocalDate,
    hasComment: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable(onClick = onClick),
        ) {
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.planting_photo),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            if (hasComment) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = stringResource(R.string.planting_photo_comment),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape,
                        ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text(
            text = stringResource(R.string.planting_photo_taken_date, takenDate.year, takenDate.monthNumber, takenDate.dayOfMonth),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDetailBottomSheet(
    imageUri: Uri,
    initialDate: LocalDate,
    initialComment: String,
    onSave: (date: LocalDate, comment: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editedDate by remember { mutableStateOf(initialDate) }
    var editedComment by remember { mutableStateOf(initialComment) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFullScreenPhoto by remember { mutableStateOf(false) }

    val canSave = editedDate != initialDate || editedComment != initialComment

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Text(
                text = stringResource(R.string.planting_photo_detail),
                style = MaterialTheme.typography.titleMedium,
            )

            // Photo preview
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.planting_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFullScreenPhoto = true },
                contentScale = ContentScale.Fit,
            )

            // Date selector
            Text(
                text = stringResource(R.string.planting_photo_date),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = stringResource(R.string.planting_select_date),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.planting_date_format, editedDate.year, editedDate.monthNumber, editedDate.dayOfMonth),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            // Comment
            Text(
                text = stringResource(R.string.planting_photo_comment),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            OutlinedTextField(
                value = editedComment,
                onValueChange = { editedComment = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.planting_photo_comment_hint)) },
                minLines = 2,
                maxLines = 4,
            )

            // Save button
            Button(
                onClick = { onSave(editedDate, editedComment) },
                modifier = Modifier.fillMaxWidth(),
                enabled = canSave,
            ) {
                Text(stringResource(R.string.save))
            }

            HorizontalDivider()

            // Delete button
            TextButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.planting_photo_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (showDatePicker) {
        PhotoDatePickerDialog(
            initialDate = editedDate,
            onDateSelected = { date ->
                editedDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showFullScreenPhoto) {
        FullScreenPhotoViewer(
            model = imageUri,
            contentDescription = stringResource(R.string.planting_photo),
            onDismiss = { showFullScreenPhoto = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoDatePickerDialog(
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
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
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
        title = { Text(stringResource(R.string.planting_select_crop)) },
        text = {
            if (crops.isEmpty()) {
                Text(
                    text = stringResource(R.string.planting_no_crops),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    crops.forEach { crop ->
                        CropItem(
                            crop = crop,
                            isSelected = crop.id == selectedCrop?.id,
                            onClick = { onCropSelected(crop) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
    val cropColor = parseColorSafe(crop.colorHex, MaterialTheme.colorScheme.primary)

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
                    contentDescription = stringResource(R.string.planting_selected),
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
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HarvestDialog(
    onContinue: (LocalDate) -> Unit,
    onFinal: (LocalDate) -> Unit,
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
                        onFinal(localDate)
                    }
                }
            ) {
                Text(
                    stringResource(R.string.planting_harvest_final),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.UTC).date
                            onContinue(localDate)
                        }
                    }
                ) {
                    Text(stringResource(R.string.planting_harvest_continue))
                }
            }
        },
    ) {
        Column {
            Text(
                text = stringResource(R.string.planting_harvest_date_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun HarvestHistorySection(
    harvests: List<Harvest>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        harvests.forEachIndexed { index, harvest ->
            val harvestNumber = harvests.size - index
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.planting_harvest_count, harvestNumber),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(
                            R.string.planting_date_format,
                            harvest.harvestedDate.year,
                            harvest.harvestedDate.monthNumber,
                            harvest.harvestedDate.dayOfMonth,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun RotationWarningCard(
    warnings: List<RotationWarning>,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = stringResource(R.string.planting_warning),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.planting_rotation_warning),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            warnings.forEach { warning ->
                WarningItem(warning = warning)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.planting_rotation_continue),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun WarningItem(
    warning: RotationWarning,
) {
    val backgroundColor = when (warning.severity) {
        WarningSeverity.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        WarningSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
    }

    val borderColor = when (warning.severity) {
        WarningSeverity.HIGH -> MaterialTheme.colorScheme.error
        WarningSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp),
            )
            .border(
                width = 1.dp,
                color = borderColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
            )
            .padding(12.dp),
    ) {
        Text(
            text = warning.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
