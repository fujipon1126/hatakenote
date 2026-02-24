package com.example.hatakenote.core.database.di

import android.content.Context
import androidx.room.Room
import com.example.hatakenote.core.database.HatakeDatabase
import com.example.hatakenote.core.database.InitialDataCallback
import com.example.hatakenote.core.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HatakeDatabase {
        lateinit var database: HatakeDatabase
        database = Room.databaseBuilder(
            context,
            HatakeDatabase::class.java,
            "hatake_database"
        )
            .addCallback(InitialDataCallback { database })
            .build()
        return database
    }

    @Provides
    fun provideCropFamilyDao(database: HatakeDatabase): CropFamilyDao =
        database.cropFamilyDao()

    @Provides
    fun provideCropDao(database: HatakeDatabase): CropDao =
        database.cropDao()

    @Provides
    fun provideFertilizerScheduleDao(database: HatakeDatabase): FertilizerScheduleDao =
        database.fertilizerScheduleDao()

    @Provides
    fun provideRotationIncompatibilityDao(database: HatakeDatabase): RotationIncompatibilityDao =
        database.rotationIncompatibilityDao()

    @Provides
    fun providePlotDao(database: HatakeDatabase): PlotDao =
        database.plotDao()

    @Provides
    fun providePlantingDao(database: HatakeDatabase): PlantingDao =
        database.plantingDao()

    @Provides
    fun providePlantingPhotoDao(database: HatakeDatabase): PlantingPhotoDao =
        database.plantingPhotoDao()

    @Provides
    fun provideWorkLogDao(database: HatakeDatabase): WorkLogDao =
        database.workLogDao()

    @Provides
    fun provideReminderDao(database: HatakeDatabase): ReminderDao =
        database.reminderDao()
}
