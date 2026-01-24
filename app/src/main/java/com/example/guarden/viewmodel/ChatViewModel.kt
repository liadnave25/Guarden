package com.example.guarden.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.guarden.data.PlantDao
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val plantDao: PlantDao
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val apiKey = "Your_API_Key"

    private val generativeModel = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = apiKey
    )

    private var chatSession: Chat? = null

    fun sendMessage(userQuestion: String) {
        val userMessage = ChatMessage(text = userQuestion, isFromUser = true)
        _messages.value += userMessage
        _isLoading.value = true

        viewModelScope.launch {
            try {
                if (chatSession == null) {
                    initializeChatSession()
                }

                val response = chatSession?.sendMessage(userQuestion)

                response?.text?.let { botResponse ->
                    val botMessage = ChatMessage(text = botResponse, isFromUser = false)
                    _messages.value += botMessage
                }
            } catch (e: Exception) {
                _messages.value += ChatMessage("Error: ${e.localizedMessage ?: "Unknown error"}", false)

            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun initializeChatSession() {
        val plants = plantDao.getPlants().first()
        val plantsContext = if (plants.isEmpty()) {
            "None (The user garden is empty)."
        } else {
            plants.joinToString(", ") { "${it.name} (Type: ${it.type})" }
        }

        val systemInstruction = """
            You are 'Guarden AI', an expert nursery consultant.
            
            --- CONTEXT: CURRENT GARDEN ---
            The user ALREADY owns these plants:
            [$plantsContext]
            
            --- LOGIC & INSTRUCTIONS ---
            1. **NEW PLANT RECOMMENDATIONS:**
               - Do NOT recommend plants already owned.
               - You MUST collect ALL 4 pieces of info before recommending:
                 a) Sunlight exposure
                 b) Location
                 c) Watering commitment
                 d) Desired Size
               - If info is missing, ask for it. DO NOT recommend yet.
            
            2. **POTS & PLANTERS:** Ask for size, color, and specific plant.
            3. **CARE:** Give specific advice for owned plants.
            4. **TONE:** Friendly, emojis 🌱, short answers.
            
            IMPORTANT: Remember the conversation history. If the user answers a question you asked, use that new info combined with previous info to give the recommendation.
        """.trimIndent()


        chatSession = generativeModel.startChat(
            history = listOf(
                content(role = "user") { text(systemInstruction) },
                content(role = "model") { text("Understood. I am Guarden AI. How can I help you with your garden today?") }
            )
        )
    }
}