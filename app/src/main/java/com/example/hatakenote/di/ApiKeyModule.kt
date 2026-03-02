package com.example.hatakenote.di

import com.example.hatakenote.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {

    @Provides
    @Singleton
    @Named("gemini_api_key")
    fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY
}
