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

/**
 * 背景色に対して読みやすい前景色（白 or 黒）を返す。
 * W3Cの相対輝度の計算に基づき、明るい背景なら黒、暗い背景なら白を返す。
 */
fun contrastTextColor(backgroundColor: Color): Color {
    val luminance = 0.299 * backgroundColor.red +
            0.587 * backgroundColor.green +
            0.114 * backgroundColor.blue
    return if (luminance > 0.5) Color.Black else Color.White
}
