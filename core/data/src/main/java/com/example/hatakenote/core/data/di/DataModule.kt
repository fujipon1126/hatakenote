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

    // Note: CropRepository, PlotRepository, PlantingRepository, WorkLogRepository,
    // ReminderRepository, CropFamilyRepository, FertilizerScheduleRepository,
    // RotationIncompatibilityRepository, PlantingPhotoRepository are now bound in FirestoreModule

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiAssistantRepository(
        impl: AiAssistantRepositoryImpl
    ): AiAssistantRepository

    @Binds
    @Singleton
    abstract fun bindEntityLastViewedRepository(
        impl: EntityLastViewedRepositoryImpl
    ): EntityLastViewedRepository
}
