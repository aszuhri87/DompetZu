package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiChatModel
import com.example.data.gemini.GeminiRepository
import com.example.data.gemini.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val initialWelcomeMessage = ChatMessage(
        role = MessageRole.MODEL,
        text = "Halo! Saya Asisten Keuangan DompetZu didukung oleh Google Gemini AI. 🤖💡\n\nSaya siap membantu Anda menganalisis pengeluaran, menyusun strategi berhemat, merencanakan anggaran, dan menjawab pertanyaan seputar finansial pribadi Anda. Apa yang ingin Anda diskusikan hari ini?",
        modelUsed = GeminiChatModel.LITE
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(initialWelcomeMessage))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedModel = MutableStateFlow(GeminiChatModel.LITE)
    val selectedModel: StateFlow<GeminiChatModel> = _selectedModel.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun selectModel(model: GeminiChatModel) {
        _selectedModel.value = model
    }

    fun sendMessage(userText: String, financialContext: String? = null) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isLoading.value) return

        val userMessage = ChatMessage(
            role = MessageRole.USER,
            text = trimmed
        )

        val currentList = _messages.value
        _messages.value = currentList + userMessage
        _inputText.value = ""
        _isLoading.value = true

        val currentModel = _selectedModel.value

        viewModelScope.launch {
            val result = repository.sendChatMessage(
                history = currentList,
                userMessage = trimmed,
                model = currentModel,
                financialContext = financialContext
            )

            result.onSuccess { reply ->
                val assistantMessage = ChatMessage(
                    role = MessageRole.MODEL,
                    text = reply,
                    modelUsed = currentModel
                )
                _messages.value = _messages.value + assistantMessage
                _isLoading.value = false
            }.onFailure { error ->
                val errorMessage = ChatMessage(
                    role = MessageRole.MODEL,
                    text = "Maaf, terjadi kendala saat memproses permintaan: ${error.localizedMessage ?: "Koneksi bermasalah"}. Silakan periksa koneksi internet atau kunci API Anda dan coba lagi.",
                    isError = true,
                    modelUsed = currentModel
                )
                _messages.value = _messages.value + errorMessage
                _isLoading.value = false
            }
        }
    }

    fun clearHistory() {
        _messages.value = listOf(initialWelcomeMessage)
    }
}
