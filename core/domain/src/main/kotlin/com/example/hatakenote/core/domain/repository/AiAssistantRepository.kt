package com.example.hatakenote.core.domain.repository

interface AiAssistantRepository {
    suspend fun sendMessage(message: String): Result<String>
}
