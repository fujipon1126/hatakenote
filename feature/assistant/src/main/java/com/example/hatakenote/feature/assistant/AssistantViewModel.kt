package com.example.hatakenote.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hatakenote.core.domain.repository.AiAssistantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isFromUser: Boolean,
    val isError: Boolean = false,
)

data class AssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val aiAssistantRepository: AiAssistantRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val message = _uiState.value.inputText.trim()
        if (message.isEmpty() || _uiState.value.isLoading) return

        // Add user message
        val userMessage = ChatMessage(
            content = message,
            isFromUser = true,
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isLoading = true,
        )

        viewModelScope.launch {
            aiAssistantRepository.sendMessage(message)
                .onSuccess { response ->
                    val aiMessage = ChatMessage(
                        content = response,
                        isFromUser = false,
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMessage,
                        isLoading = false,
                    )
                }
                .onFailure { error ->
                    val errorMessage = ChatMessage(
                        content = "エラーが発生しました: ${error.message ?: "不明なエラー"}",
                        isFromUser = false,
                        isError = true,
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorMessage,
                        isLoading = false,
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
