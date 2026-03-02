package com.example.hatakenote.core.ui.util

import androidx.compose.ui.graphics.Color

/**
 * 16進数カラーコードをComposeのColorに安全に変換する。
 * パースに失敗した場合はデフォルト色を返す。
 *
 * @param colorHex 16進数カラーコード（例: "#FF5733"）
 * @param default パース失敗時のデフォルト色
 * @return パースされたColor、または失敗時はdefault
 */
fun parseColorSafe(colorHex: String, default: Color = Color.Gray): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        default
    }
}
