package com.example.hatakenote.feature.crop

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.example.hatakenote.core.ui.component.FullScreenPhotoViewer
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.hatakenote.core.domain.model.Crop
import com.example.hatakenote.core.domain.model.CropFamily
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.ui.util.parseColorSafe

@Composable
internal fun CropDetailRoute(
    onBackClick: () -> Unit,
    onPlantingClick: (Long) -> Unit,
    viewModel: CropDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshData()
        onPauseOrDispose {}
    }

    CropDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onPlantingClick = onPlantingClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CropDetailScreen(
    uiState: CropDetailUiState,
    onBackClick: () -> Unit,
    onPlantingClick: (Long) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.crop?.name ?: stringResource(R.string.crop_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.crop_back),
                        )
                    }
                },
            )
        },
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
        } else if (uiState.crop == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.crop_detail_not_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                CropInfoCard(crop = uiState.crop, family = uiState.family)

                Spacer(modifier = Modifier.height(16.dp))

                CurrentPlantingsSection(
                    plantings = uiState.activePlantings,
                    onPlantingClick = onPlantingClick,
                )

                Spacer(modifier = Modifier.height(16.dp))

                PastPlantingsSection(
                    plantings = uiState.pastPlantings,
                    onPlantingClick = onPlantingClick,
                )

                Spacer(modifier = Modifier.height(16.dp))

                val allPhotos = (uiState.activePlantings + uiState.pastPlantings)
                    .flatMap { it.photos }
                    .sortedByDescending { it.takenDate }
                PhotosSection(photos = allPhotos)
            }
        }
    }
}

@Composable
private fun CropInfoCard(crop: Crop, family: CropFamily?) {
    val cropColor = parseColorSafe(crop.colorHex, MaterialTheme.colorScheme.primary)

    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(cropColor),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (family != null) {
                    Text(
                        text = stringResource(R.string.crop_detail_family, family.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.crop_detail_rotation_years, family.rotationYears),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrentPlantingsSection(
    plantings: List<PlantingWithPlots>,
    onPlantingClick: (Long) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.crop_detail_current_plantings),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (plantings.isEmpty()) {
            EmptyCard(stringResource(R.string.crop_detail_no_current_plantings))
        } else {
            plantings.forEach { plantingWithPlots ->
                PlantingCard(
                    plantingWithPlots = plantingWithPlots,
                    onClick = { onPlantingClick(plantingWithPlots.planting.id) },
                    showHarvestedDate = false,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PastPlantingsSection(
    plantings: List<PlantingWithPlots>,
    onPlantingClick: (Long) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.crop_detail_harvest_history),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (plantings.isEmpty()) {
            EmptyCard(stringResource(R.string.crop_detail_no_harvest_history))
        } else {
            plantings.forEach { plantingWithPlots ->
                PlantingCard(
                    plantingWithPlots = plantingWithPlots,
                    onClick = { onPlantingClick(plantingWithPlots.planting.id) },
                    showHarvestedDate = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PlantingCard(
    plantingWithPlots: PlantingWithPlots,
    onClick: () -> Unit,
    showHarvestedDate: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (showHarvestedDate) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(
                    R.string.crop_detail_planted_date,
                    plantingWithPlots.planting.plantedDate.toString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (showHarvestedDate && plantingWithPlots.planting.harvestedDate != null) {
                Text(
                    text = stringResource(
                        R.string.crop_detail_harvested_date,
                        plantingWithPlots.planting.harvestedDate.toString(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (plantingWithPlots.plotNames.isNotEmpty()) {
                Text(
                    text = stringResource(
                        R.string.crop_detail_plots,
                        plantingWithPlots.plotNames.joinToString(", "),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PhotosSection(photos: List<PlantingPhoto>) {
    var fullScreenPhotoUri by remember { mutableStateOf<Uri?>(null) }

    Column {
        Text(
            text = stringResource(R.string.crop_detail_photos),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (photos.isEmpty()) {
            EmptyCard(stringResource(R.string.crop_detail_no_photos))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(photos) { photo ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AsyncImage(
                            model = Uri.parse(photo.filePath),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { fullScreenPhotoUri = Uri.parse(photo.filePath) },
                            contentScale = ContentScale.Crop,
                        )
                        Text(
                            text = stringResource(
                                R.string.crop_detail_photo_date,
                                photo.takenDate.year,
                                photo.takenDate.monthNumber,
                                photo.takenDate.dayOfMonth,
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    fullScreenPhotoUri?.let { uri ->
        FullScreenPhotoViewer(
            model = uri,
            contentDescription = null,
            onDismiss = { fullScreenPhotoUri = null },
        )
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Grass,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
