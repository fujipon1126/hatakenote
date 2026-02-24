package com.example.hatakenote.core.data.di

import com.example.hatakenote.core.data.repository.*
import com.example.hatakenote.core.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCropFamilyRepository(
        impl: CropFamilyRepositoryImpl
    ): CropFamilyRepository

    @Binds
    @Singleton
    abstract fun bindCropRepository(
        impl: CropRepositoryImpl
    ): CropRepository

    @Binds
    @Singleton
    abstract fun bindPlotRepository(
        impl: PlotRepositoryImpl
    ): PlotRepository

    @Binds
    @Singleton
    abstract fun bindPlantingRepository(
        impl: PlantingRepositoryImpl
    ): PlantingRepository

    @Binds
    @Singleton
    abstract fun bindWorkLogRepository(
        impl: WorkLogRepositoryImpl
    ): WorkLogRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindRotationIncompatibilityRepository(
        impl: RotationIncompatibilityRepositoryImpl
    ): RotationIncompatibilityRepository

    @Binds
    @Singleton
    abstract fun bindFertilizerScheduleRepository(
        impl: FertilizerScheduleRepositoryImpl
    ): FertilizerScheduleRepository

    @Binds
    @Singleton
    abstract fun bindPlantingPhotoRepository(
        impl: PlantingPhotoRepositoryImpl
    ): PlantingPhotoRepository
}
