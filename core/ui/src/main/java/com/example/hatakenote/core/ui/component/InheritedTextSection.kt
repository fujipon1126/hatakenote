@file:OptIn(ExperimentalFoundationStyleApi::class, ExperimentalFoundationApi::class)

package com.example.hatakenote.core.ui.component

import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.MutableStyleState
import androidx.compose.foundation.style.styleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.hatakenote.core.ui.theme.SectionTextStyle

/**
 * Styles API の「スタイル継承（cascade）」を体現するデモコンテナ。
 *
 * 親（この Column）に [SectionTextStyle]（contentColor / textStyle）を `styleable` で宣言すると、
 * 内部の [androidx.compose.foundation.text.BasicText]（明示スタイル未指定）が
 * その色・書体を**継承**する。従来は `CompositionLocalProvider(LocalContentColor provides ...)` 等の
 * 手動配線が必要だった。
 *
 * 注意:
 * - 継承は実験的機能で `ComposeFoundationFlags.isInheritedTextStyleEnabled` の有効化が前提。
 * - Material3 の `Text` は自前で色を解決するため継承しない。継承を効かせるには
 *   明示スタイルを与えない `BasicText` を使う。
 */
@Composable
fun InheritedTextSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // 継承のopt-in（冪等）。本来はApplication起動時に一度設定するのが望ましい。
    ComposeFoundationFlags.isInheritedTextStyleEnabled = true

    val styleState = remember { MutableStyleState(null) }
    Column(modifier = modifier.styleable(styleState, SectionTextStyle)) {
        content()
    }
}
