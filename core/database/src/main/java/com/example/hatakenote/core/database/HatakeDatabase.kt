package com.example.hatakenote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.hatakenote.core.database.converter.Converters
import com.example.hatakenote.core.database.dao.*
import com.example.hatakenote.core.database.entity.*

@Database(
    entities = [
        CropFamilyEntity::class,
        CropEntity::class,
        FertilizerScheduleEntity::class,
        RotationIncompatibilityEntity::class,
        PlotEntity::class,
        PlantingEntity::class,
        PlantingPlotCrossRef::class,
        PlantingPhotoEntity::class,
        WorkLogEntity::class,
        ReminderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HatakeDatabase : RoomDatabase() {
    abstract fun cropFamilyDao(): CropFamilyDao
    abstract fun cropDao(): CropDao
    abstract fun fertilizerScheduleDao(): FertilizerScheduleDao
    abstract fun rotationIncompatibilityDao(): RotationIncompatibilityDao
    abstract fun plotDao(): PlotDao
    abstract fun plantingDao(): PlantingDao
    abstract fun plantingPhotoDao(): PlantingPhotoDao
    abstract fun workLogDao(): WorkLogDao
    abstract fun reminderDao(): ReminderDao
}
