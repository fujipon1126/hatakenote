@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.example.hatakenote.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Jetpack Compose Foundation の Styles API（実験的 / Google I/O 2026 で紹介）で定義した独自スタイルトークン。
 *
 * 従来の [HatakeShapes] 等は CompositionLocal でトークンを供給するだけだったが、
 * Styles API では「背景・形状・パディングなどの複数スタイルをひとまとまりの [Style] として再利用」できる。
 * ここでは作物チップ用に、pill 形状＋左右パディング＋背景を1つのトークンに束ねる。
 */
val CropChipStyle: Style = Style {
    shape(RoundedCornerShape(percent = 50))
    contentPaddingStart(8.dp)
    contentPaddingEnd(8.dp)
    contentPaddingTop(4.dp)
    contentPaddingBottom(4.dp)
    background(HatakeStyleColors.cropChipBackground)
}

/**
 * [StyleScope] 拡張による独自スタイルプロパティ。
 * Styles API では拡張関数でスタイルの語彙そのものを増やせる。
 * ここでは「[color] の実線枠 ＋ 同色の薄いティント背景」をまとめて適用し、
 * 枠線が背景に埋もれない“outlined”な見た目を1関数で表現する。
 */
fun StyleScope.outlinedBackground(color: Color) {
    borderWidth(2.dp)
    borderColor(color)
    background(color.copy(alpha = 0.12f))
}

/** Style 定義で使う raw カラー（テーマ非依存の素値）。 */
private object HatakeStyleColors {
    val cropChipBackground = Color(0xFF388E3C)
}
