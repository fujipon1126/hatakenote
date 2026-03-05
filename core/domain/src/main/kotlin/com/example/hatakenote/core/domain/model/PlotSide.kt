package com.example.hatakenote.core.domain.model

enum class PlotSide {
    LEFT, RIGHT;

    fun displayName(): String = when (this) {
        LEFT -> "左"
        RIGHT -> "右"
    }
}
