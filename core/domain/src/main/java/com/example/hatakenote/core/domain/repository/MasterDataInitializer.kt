package com.example.hatakenote.core.domain.repository

interface MasterDataInitializer {
    suspend fun initializeIfNeeded()
}
