package com.example.hatakenote.core.domain.model

/**
 * アプリ設定
 */
data class AppSettings(
    val latitude: Double = DEFAULT_LATITUDE,
    val longitude: Double = DEFAULT_LONGITUDE,
    val locationName: String = DEFAULT_LOCATION_NAME,
    val reminderNotifyDaysBefore: Int = DEFAULT_REMINDER_NOTIFY_DAYS,
) {
    companion object {
        // デフォルト：八王子
        const val DEFAULT_LATITUDE = 35.6662
        const val DEFAULT_LONGITUDE = 139.3160
        const val DEFAULT_LOCATION_NAME = "八王子"
        const val DEFAULT_REMINDER_NOTIFY_DAYS = 3
    }
}
