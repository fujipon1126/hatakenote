@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.example.hatakenote.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.hatakenote.core.ui.theme.outlinedBackground

/**
 * Styles API の独自スタイルプロパティ [outlinedBackground] を使った枠線付き作物チップ。
 *
 * [CropChip]（固定緑・塗りつぶし）と異なり、色を実行時に受け取り、
 * `Style { ... outlinedBackground(color) }` を組み立てて `Modifier.styleable` で適用する。
 * これにより作物ごとの色で outlined チップを描ける。
 */
@Composable
fun OutlinedCropChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val style = remember(color) {
        Style {
            shape(RoundedCornerShape(percent = 50))
            contentPaddingStart(8.dp)
            contentPaddingEnd(8.dp)
            contentPaddingTop(4.dp)
            contentPaddingBottom(4.dp)
            outlinedBackground(color)
        }
    }
    val styleState = remember { MutableStyleState(null) }
    Box(modifier = modifier.styleable(styleState, style)) {
        Text(
            text = text,
            color = color,
        )
    }
}
