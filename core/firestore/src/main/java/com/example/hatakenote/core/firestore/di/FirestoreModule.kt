package com.example.hatakenote.core.firestore.di

import com.example.hatakenote.core.domain.repository.CropRepository
import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.domain.repository.PlantingRepository
import com.example.hatakenote.core.domain.repository.PlotRepository
import com.example.hatakenote.core.domain.repository.ReminderRepository
import com.example.hatakenote.core.domain.repository.WorkLogRepository
import com.example.hatakenote.core.firestore.FirestoreCropRepository
import com.example.hatakenote.core.firestore.FirestoreFarmRepository
import com.example.hatakenote.core.firestore.FirestorePlantingRepository
import com.example.hatakenote.core.firestore.FirestorePlotRepository
import com.example.hatakenote.core.firestore.FirestoreReminderRepository
import com.example.hatakenote.core.firestore.FirestoreWorkLogRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirestoreProviderModule {
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirestoreBindsModule {
    @Binds
    @Singleton
    abstract fun bindFarmRepository(impl: FirestoreFarmRepository): FarmRepository

    @Binds
    @Singleton
    abstract fun bindCropRepository(impl: FirestoreCropRepository): CropRepository

    @Binds
    @Singleton
    abstract fun bindPlotRepository(impl: FirestorePlotRepository): PlotRepository

    @Binds
    @Singleton
    abstract fun bindPlantingRepository(impl: FirestorePlantingRepository): PlantingRepository

    @Binds
    @Singleton
    abstract fun bindWorkLogRepository(impl: FirestoreWorkLogRepository): WorkLogRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: FirestoreReminderRepository): ReminderRepository
}
