package com.example.hatakenote.core.firestore.di

import com.example.hatakenote.core.domain.repository.FarmRepository
import com.example.hatakenote.core.firestore.FirestoreFarmRepository
import com.google.firebase.auth.FirebaseAuth
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
}
