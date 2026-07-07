@file:OptIn(ExperimentalFoundationStyleApi::class)

package com.example.hatakenote.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.hatakenote.core.ui.theme.SelectableCropChipStyle

/**
 * Styles API の「状態連動スタイル」を体現する選択可能な作物チップ。
 *
 * ポイント（従来との違い）:
 * - 押下フィードバックと選択状態の見た目は [SelectableCropChipStyle] 側に `pressed { }` / `selected { }` として
 *   宣言済み。この Composable では **状態に応じた背景/枠線の分岐を一切書いていない**。
 * - [rememberUpdatedStyleState] が [MutableInteractionSource] を購読して押下状態を自動反映し、
 *   `setSelected(selected)` でアプリ制御の選択状態を注入するだけ。
 * - 従来は `interactionSource.collectIsPressedAsState()` ＋ `if (pressed) ...`、
 *   `if (selected) ...` を手書きする必要があった部分が不要になる。
 */
@Composable
fun SelectableCropChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val styleState = rememberUpdatedStyleState(interactionSource) {
        it.isSelected = selected
    }
    Box(
        modifier = modifier
            .styleable(styleState, SelectableCropChipStyle)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        // 文字色のみ選択状態で切り替え（背景・枠線・押下は Style 側が担当）
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF388E3C),
        )
    }
}
