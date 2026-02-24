package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.Plot

@Entity(tableName = "plots")
data class PlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gridX: Int,
    val gridY: Int,
    val width: Int = 1,
    val height: Int = 1,
)

fun PlotEntity.toDomain(): Plot = Plot(
    id = id,
    name = name,
    gridX = gridX,
    gridY = gridY,
    width = width,
    height = height,
)

fun Plot.toEntity(): PlotEntity = PlotEntity(
    id = id,
    name = name,
    gridX = gridX,
    gridY = gridY,
    width = width,
    height = height,
)
