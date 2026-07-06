@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.example.hatakenote.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.hatakenote.core.ui.theme.CropChipStyle

/**
 * Jetpack Compose Foundation の Styles API（実験的）で定義した [CropChipStyle]
 * （pill 形状＋左右パディング＋緑背景）を `Modifier.styleable` で適用する作物チップ。
 *
 * 従来なら Surface/Box に shape・color・padding を個別指定するところを、
 * ひとまとまりの [Style] トークンとして再利用している点が新 API の要。
 */
@Composable
fun CropChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val styleState = remember { MutableStyleState(null) }
    Box(modifier = modifier.styleable(styleState, CropChipStyle)) {
        Text(
            text = text,
            color = Color.White,
        )
    }
}
