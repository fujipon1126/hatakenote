package com.example.hatakenote.core.database.converter

import androidx.room.TypeConverter
import com.example.hatakenote.core.domain.model.WorkType
import kotlinx.datetime.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromWorkType(value: WorkType): String {
        return value.name
    }

    @TypeConverter
    fun toWorkType(value: String): WorkType {
        return WorkType.valueOf(value)
    }
}
