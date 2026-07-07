@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.example.hatakenote.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.pressed
import androidx.compose.foundation.style.selected
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

/**
 * Styles API の「状態連動スタイル」を使った選択可能チップ用トークン。
 *
 * 従来は押下フィードバックに `interactionSource.collectIsPressedAsState()`＋条件分岐、
 * 選択状態に `if (selected) ... else ...` の手配線が必要だったが、
 * Styles API では `pressed { }` / `selected { }` として **状態別スタイルを1つの [Style] に宣言**でき、
 * 適用側は状態を意識せず [androidx.compose.foundation.style.styleable] するだけでよい。
 */
val SelectableCropChipStyle: Style = Style {
    shape(RoundedCornerShape(percent = 50))
    contentPaddingStart(12.dp)
    contentPaddingEnd(12.dp)
    contentPaddingTop(6.dp)
    contentPaddingBottom(6.dp)
    // 既定（未選択）：実線枠のみ
    borderWidth(1.dp)
    borderColor(HatakeStyleColors.cropChipBackground)
    // 押下中：うっすら塗り（InteractionSource 連動。手動の状態監視は不要）
    pressed {
        background(HatakeStyleColors.cropChipBackground.copy(alpha = 0.2f))
    }
    // 選択中：塗りつぶし
    selected {
        background(HatakeStyleColors.cropChipBackground)
    }
}

/**
 * スタイル継承（cascade）デモ用のセクションスタイル。
 *
 * Styles API では、親の [androidx.compose.foundation.style.styleable] に宣言した
 * contentColor / textStyle などを **子孫テキストが継承**できる（CSS のカスケードに近い）。
 * 従来は LocalContentColor / LocalTextStyle を CompositionLocalProvider で
 * 手動配線する必要があった。
 * ※ 継承には `ComposeFoundationFlags.isInheritedTextStyleEnabled = true` の opt-in が必要。
 */
val SectionTextStyle: Style = Style {
    contentColor(HatakeStyleColors.cropChipBackground)
    textStyle(
        TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        ),
    )
}

/** Style 定義で使う raw カラー（テーマ非依存の素値）。 */
private object HatakeStyleColors {
    val cropChipBackground = Color(0xFF388E3C)
}
