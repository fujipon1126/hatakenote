package com.example.hatakenote.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hatakenote.core.domain.model.Plot
import com.example.hatakenote.core.domain.model.PlotSide

@Entity(tableName = "plots")
data class PlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val side: String,
    val number: Int,
    val width: Int = 1,
    val height: Int = 1,
)

fun PlotEntity.toDomain(): Plot = Plot(
    id = id,
    name = name,
    side = PlotSide.valueOf(side),
    number = number,
    width = width,
    height = height,
)

fun Plot.toEntity(): PlotEntity = PlotEntity(
    id = id,
    name = name,
    side = side.name,
    number = number,
    width = width,
    height = height,
)
