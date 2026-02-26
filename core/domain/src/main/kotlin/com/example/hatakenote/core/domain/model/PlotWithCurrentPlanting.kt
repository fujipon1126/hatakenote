package com.example.hatakenote.core.domain.model

/**
 * 区画と現在の作付け情報
 * グリッドマップ表示で使用
 */
data class PlotWithCurrentPlanting(
    val plot: Plot,
    val currentPlantings: List<PlantingWithCrop>,
)

/**
 * 作付けと作物情報
 */
data class PlantingWithCrop(
    val planting: Planting,
    val crop: Crop,
)
