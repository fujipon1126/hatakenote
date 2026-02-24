package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "planting_plot_cross_ref",
    primaryKeys = ["plantingId", "plotId"],
    foreignKeys = [
        ForeignKey(
            entity = PlantingEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["plotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("plantingId"),
        Index("plotId")
    ]
)
data class PlantingPlotCrossRef(
    val plantingId: Long,
    val plotId: Long,
)
