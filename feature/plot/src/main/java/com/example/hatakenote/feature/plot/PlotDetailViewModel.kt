package com.example.hatakenote.feature.plot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.hatakenote.core.domain.model.Planting
import com.example.hatakenote.core.domain.model.PlantingPhoto
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide
import com.example.hatakenote.core.domain.model.PlantingWithCrop
import com.example.hatakenote.core.domain.model.WorkLog
import com.example.hatakenote.core.domain.model.Fertilizer
import com.example.hatakenote.core.domain.model.Harvest
import com.example.hatakenote.core.domain.repository.AuthRepository
import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.EntityLastViewedRepository
import com.example.hatakenote.core.domain.repository.FertilizerRepository
import com.example.hatakenote.core.domain.repository.HarvestRepository
import com.example.hatakenote.core.domain.repository.PlantingPhotoRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.ViewedEntityType
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import com.example.hatakenote.core.domain.usecase.GetRotationAdviceUseCase
import com.example.hatakenote.core.domain.usecase.RotationAdvice
import com.example.hatakenote.feature.plot.navigation.PlotDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject

data class PlotDetailUiState(
    val plot: Plot? = null,
    val siblingPlots: List<Plot> = emptyList(),
    val currentPlantings: List<PlantingWithCrop> = emptyList(),
    val pastPlantings: List<PlantingWithCrop> = emptyList(),
    val workLogs: List<WorkLog> = emptyList(),
    val fertilizerMap: Map<Long, Fertilizer> = emptyMap(),
    val photosByPlantingId: Map<Long, List<PlantingPhoto>> = emptyMap(),
    val newPlantingIds: Set<Long> = emptySet(),
    val newPastPlantingIds: Set<Long> = emptySet(),
    val newWorkLogIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val showEditDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val editDialogError: String? = null,
    val plantingToDelete: PlantingWithCrop? = null,
    val rotationAdvice: RotationAdvice? = null,
)

@HiltViewModel
class PlotDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val plotRepository: PlotRepository,
    private val plantingRepository: PlantingRepository,
    private val cropRepository: CropRepository,
    private val workLogRepository: WorkLogRepository,
    private val fertilizerRepository: FertilizerRepository,
    private val plantingPhotoRepository: PlantingPhotoRepository,
    private val harvestRepository: HarvestRepository,
    private val getRotationAdviceUseCase: GetRotationAdviceUseCase,
    private val entityLastViewedRepository: EntityLastViewedRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<PlotDetailRoute>()
    val plotId: Long = route.plotId

    private val _uiState = MutableStateFlow(PlotDetailUiState())
    val uiState: StateFlow<PlotDetailUiState> = _uiState.asStateFlow()

    // 収穫履歴一括既読化のために、画面表示中の pastPlanting / harvest の ID を保持。
    private var displayedPastPlantingIds: List<Long> = emptyList()
    private var displayedHarvestIds: List<Long> = emptyList()

    init {
        loadPlotDetails()
    }

    private fun loadPlotDetails() {
        viewModelScope.launch {
            val plotWithPlantings = plotRepository.getByIdWithCurrentPlantings(plotId)
            if (plotWithPlantings != null) {
                // Get past plantings (inactive)
                val allPlantings = plantingRepository.getHistoryByPlotId(plotId).first()
                val pastPlantings = allPlantings.filter { !it.isActive }

                // 兄弟区画（同じ作付けを共有する区画）のIDを収集
                val plantingIds = plotWithPlantings.currentPlantings.map { it.planting.id }
                val siblingPlotIds = mutableSetOf(plotId)
                for (plantingId in plantingIds) {
                    val plotIds = plantingRepository.getPlotIdsForPlanting(plantingId)
                    siblingPlotIds.addAll(plotIds)
                }

                // 兄弟区画のPlotオブジェクトを取得（名前表示用）
                val allPlots = plotRepository.getAll().first()
                val siblingPlots = siblingPlotIds
                    .filter { it != plotId }
                    .mapNotNull { sibId -> allPlots.find { it.id == sibId } }
                    .sortedWith(compareBy({ it.side }, { it.number }))

                // 全兄弟区画の plot-bound 作業記録を取得（TILL, BASE_FERTILIZE）
                val plotWorkLogs = siblingPlotIds.flatMap { sibPlotId ->
                    workLogRepository.getByPlotId(sibPlotId).first()
                }

                // planting-bound 作業記録を取得（FERTILIZE, OTHER）
                val plantingWorkLogs = plantingIds.flatMap { plantingId ->
                    workLogRepository.getByPlantingId(plantingId).first()
                }

                val workLogs = (plotWorkLogs + plantingWorkLogs)
                    .distinctBy { it.id }
                    .sortedWith(compareByDescending<WorkLog> { it.workDate }.thenByDescending { it.id })

                val rotationAdvice = getRotationAdviceUseCase(plotId)

                // 肥料マスターをMapとして取得
                val fertilizers = fertilizerRepository.getAll().first()
                val fertilizerMap = fertilizers.associateBy { it.id }

                // 現在の作付けに紐づく写真を取得（plantingId経由 + plotId経由）
                val photosByPlantingId = mutableMapOf<Long, List<PlantingPhoto>>()
                val plotPhotos = plantingPhotoRepository.getByPlotId(plotId).first()
                // 区画写真のうち、作付けに紐づいていないもの（区画全体の写真）
                val plotOnlyPhotos = plotPhotos.filter { it.plantingId == null }
                for (pwc in plotWithPlantings.currentPlantings) {
                    val pid = pwc.planting.id
                    val plantingPhotos = plantingPhotoRepository.getByPlantingId(pid).first()
                    val combined = (plantingPhotos + plotOnlyPhotos)
                        .distinctBy { it.id }
                        .sortedWith(
                            compareByDescending<PlantingPhoto> { it.takenDate }
                                .thenByDescending { it.id }
                        )
                    if (combined.isNotEmpty()) {
                        photosByPlantingId[pid] = combined
                    }
                }

                val pastPlantingsWithCrop = pastPlantings.mapNotNull { planting ->
                    val crop = cropRepository.getById(planting.cropId) ?: return@mapNotNull null
                    PlantingWithCrop(planting = planting, crop = crop)
                }

                // 収穫履歴ブロックで参照する harvest を集めておく
                val pastPlantingIds = pastPlantings.map { it.id }
                val harvestsForPastPlantings = pastPlantingIds.flatMap { pid ->
                    harvestRepository.getByPlantingId(pid).first()
                }

                // NEW 判定用の lastViewed と currentUserId を取得
                val plantingLastViewed = entityLastViewedRepository.lastViewed(ViewedEntityType.PLANTING).first()
                val workLogLastViewed = entityLastViewedRepository.lastViewed(ViewedEntityType.WORK_LOG).first()
                val harvestLastViewed = entityLastViewedRepository.lastViewed(ViewedEntityType.HARVEST).first()
                val currentUserId = authRepository.currentUser.first()?.id

                // 現在の作物 NEW: Planting 自身 or 紐づく PlantingPhoto の他人更新
                val photosByPlantingIdForJudge = photosByPlantingId
                val newPlantingIds = plotWithPlantings.currentPlantings
                    .filter { pwc ->
                        val planting = pwc.planting
                        val viewedAt = plantingLastViewed[planting.id]
                        val plantingNew = isUnreadOthers(planting.updatedAt, planting.updatedBy, viewedAt, currentUserId)
                        val photoNew = photosByPlantingIdForJudge[planting.id]
                            .orEmpty()
                            .any { photo ->
                                isUnreadOthers(photo.updatedAt, photo.updatedBy, viewedAt, currentUserId)
                            }
                        plantingNew || photoNew
                    }
                    .map { it.planting.id }
                    .toSet()

                // 区画作業履歴 NEW: その WorkLog の他人更新
                val newWorkLogIds = workLogs
                    .filter { wl ->
                        isUnreadOthers(wl.updatedAt, wl.updatedBy, workLogLastViewed[wl.id], currentUserId)
                    }
                    .map { it.id }
                    .toSet()

                // 収穫履歴 NEW: Planting 自身 or 紐づく Harvest の他人更新
                val harvestsByPlantingId = harvestsForPastPlantings.groupBy { it.plantingId }
                val newPastPlantingIds = pastPlantingsWithCrop
                    .filter { pwc ->
                        val planting = pwc.planting
                        val plantingNew = isUnreadOthers(
                            planting.updatedAt,
                            planting.updatedBy,
                            plantingLastViewed[planting.id],
                            currentUserId,
                        )
                        val harvestNew = harvestsByPlantingId[planting.id].orEmpty().any { h ->
                            isUnreadOthers(h.updatedAt, h.updatedBy, harvestLastViewed[h.id], currentUserId)
                        }
                        plantingNew || harvestNew
                    }
                    .map { it.planting.id }
                    .toSet()

                displayedPastPlantingIds = pastPlantingIds
                displayedHarvestIds = harvestsForPastPlantings.map { it.id }

                _uiState.value = _uiState.value.copy(
                    plot = plotWithPlantings.plot,
                    siblingPlots = siblingPlots,
                    currentPlantings = plotWithPlantings.currentPlantings,
                    pastPlantings = pastPlantingsWithCrop,
                    workLogs = workLogs,
                    fertilizerMap = fertilizerMap,
                    photosByPlantingId = photosByPlantingId,
                    newPlantingIds = newPlantingIds,
                    newPastPlantingIds = newPastPlantingIds,
                    newWorkLogIds = newWorkLogIds,
                    rotationAdvice = rotationAdvice,
                    isLoading = false,
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /** 画面退出時に呼ばれ、表示中の収穫履歴ブロック対象を一括既読化する。 */
    fun markPastPlantingsViewed() {
        val plantingIds = displayedPastPlantingIds
        val harvestIds = displayedHarvestIds
        if (plantingIds.isEmpty() && harvestIds.isEmpty()) return
        viewModelScope.launch {
            entityLastViewedRepository.markManyViewed(ViewedEntityType.PLANTING, plantingIds)
            entityLastViewedRepository.markManyViewed(ViewedEntityType.HARVEST, harvestIds)
        }
    }

    private fun isUnreadOthers(
        updatedAt: Instant,
        updatedBy: String?,
        viewedAt: Instant?,
        currentUserId: String?,
    ): Boolean {
        if (updatedBy == null) return false  // 旧データは自分扱い
        if (currentUserId != null && updatedBy == currentUserId) return false  // 自分の更新は除外
        val baseline = viewedAt ?: Instant.fromEpochMilliseconds(0)
        return updatedAt > baseline
    }

    fun refreshData() {
        loadPlotDetails()
    }

    fun showEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = true, editDialogError = null)
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, editDialogError = null)
    }

    fun showDeleteConfirmDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = true)
    }

    fun dismissDeleteConfirmDialog() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmDialog = false)
    }

    fun updatePlot(name: String, side: PlotSide, number: Int) {
        viewModelScope.launch {
            val currentPlot = _uiState.value.plot ?: return@launch

            // 重複チェック（自分自身は除く）
            val allPlots = plotRepository.getAll().first()
            val duplicate = allPlots.any {
                it.side == side && it.number == number && it.id != currentPlot.id
            }
            if (duplicate) {
                _uiState.value = _uiState.value.copy(
                    editDialogError = "${side.displayName()}${number} は登録済みです"
                )
                return@launch
            }

            val updatedPlot = currentPlot.copy(
                name = name,
                side = side,
                number = number,
            )
            plotRepository.update(updatedPlot)
            _uiState.value = _uiState.value.copy(
                plot = updatedPlot,
                showEditDialog = false,
            )
        }
    }


    fun showDeletePlantingDialog(plantingWithCrop: PlantingWithCrop) {
        _uiState.value = _uiState.value.copy(plantingToDelete = plantingWithCrop)
    }

    fun dismissDeletePlantingDialog() {
        _uiState.value = _uiState.value.copy(plantingToDelete = null)
    }

    fun deletePlanting() {
        val plantingWithCrop = _uiState.value.plantingToDelete ?: return
        viewModelScope.launch {
            plantingRepository.delete(plantingWithCrop.planting)
            _uiState.value = _uiState.value.copy(
                currentPlantings = _uiState.value.currentPlantings - plantingWithCrop,
                plantingToDelete = null,
            )
        }
    }

    fun deletePlot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val currentPlot = _uiState.value.plot ?: return@launch
            plotRepository.delete(currentPlot)
            dismissDeleteConfirmDialog()
            onDeleted()
        }
    }
}
