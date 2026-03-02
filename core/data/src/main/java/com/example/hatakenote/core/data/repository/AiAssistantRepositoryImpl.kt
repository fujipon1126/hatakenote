package com.example.hatakenote.core.data.repository

import com.example.hatakenote.core.domain.repository.AiAssistantRepository
import com.example.hatakenote.core.network.gemini.GeminiService
import javax.inject.Inject

class AiAssistantRepositoryImpl @Inject constructor(
    private val geminiService: GeminiService,
) : AiAssistantRepository {

    override suspend fun sendMessage(message: String): Result<String> {
        return geminiService.sendMessage(message)
    }
}
