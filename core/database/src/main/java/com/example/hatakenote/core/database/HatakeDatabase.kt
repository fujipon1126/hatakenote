package com.example.hatakenote.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class HatakeDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE planting_photos ADD COLUMN comment TEXT DEFAULT NULL")
            }
        }
    }

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
