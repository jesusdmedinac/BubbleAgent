package com.bubble.agent.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubble.agent.BubbleApplication
import com.bubble.agent.ai.BubbleAiEngine
import com.bubble.agent.ai.MockAiEngine
import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.OfflineProfile
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.data.repository.BubbleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class MessageSender {
    USER, BUBBLE
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val selectedTab: Int = 2,
    val userName: String = "Jesús Daniel",
    val showDashboardHeader: Boolean = true
)

class MainScreenViewModel(
    private val aiEngine: BubbleAiEngine = MockAiEngine(),
    private val repository: BubbleRepository = BubbleApplication.instance.appContainer.bubbleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> =
        repository.digitalHealthSnapshot

    val recommendedActivities: StateFlow<List<RecommendedActivity>> =
        repository.recommendedActivities

    val offlineProfile: StateFlow<OfflineProfile> =
        repository.offlineProfile

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onNewChatClicked() {
        _uiState.update { it.copy(messages = emptyList(), inputText = "") }
    }

    fun toggleDashboardHeader() {
        _uiState.update { it.copy(showDashboardHeader = !it.showDashboardHeader) }
    }

    fun onToggleActivityCompleted(id: String) {
        repository.toggleActivityCompleted(id)
    }

    fun onRefreshUsageStats() {
        repository.refreshSnapshot()
    }

    fun onOpenUsageSettings(context: Context) {
        try {
            val intent = BubbleApplication.instance.appContainer.screenTimeTracker.createSettingsIntent()
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handled gracefully
        }
    }

    fun simulateGeminiMcpInvocation() {
        val sampleActivities = listOf(
            Triple(
                "Sesión de guitarra acústica (Fingerpicking)",
                "MUSIC",
                "Gemini MCP: Detectó 1h 45m en Instagram. Reemplaza el doomscrolling por 25m de práctica musical activa."
            ),
            Triple(
                "Caminata consciente al aire libre",
                "NATURE",
                "Gemini MCP: Reduce fatiga visual de pantalla y estimula producción de dopamina natural."
            ),
            Triple(
                "Capítulo de novela de ciencia ficción",
                "READING",
                "Gemini MCP: Alternativa recomendada para sustituir tiempo nocturno de pantalla antes de dormir."
            )
        )
        val selected = sampleActivities.random()
        repository.saveRecommendedActivity(
            title = selected.first,
            category = selected.second,
            estimatedMinutes = (20..45).random(),
            reason = selected.third
        )
    }

    fun onSendMessage() {
        val query = _uiState.value.inputText.trim()
        if (query.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = query
        )

        val assistantMsgId = UUID.randomUUID().toString()
        val initialAssistantMessage = ChatMessage(
            id = assistantMsgId,
            sender = MessageSender.BUBBLE,
            text = ""
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage + initialAssistantMessage,
                inputText = "",
                isGenerating = true
            )
        }

        viewModelScope.launch {
            var accumulated = ""
            aiEngine.streamChat(query)
                .catch {
                    if (accumulated.isEmpty()) {
                        accumulated = "¡Hola Jesús! Estoy aquí en tu dispositivo de forma 100% privada y local vía Gemini Nano. ¿Qué actividad del mundo real te gustaría priorizar hoy?"
                        updateAssistantMessage(assistantMsgId, accumulated)
                    }
                }
                .collect { token ->
                    accumulated += token
                    updateAssistantMessage(assistantMsgId, accumulated)
                }

            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    private fun updateAssistantMessage(id: String, text: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == id) msg.copy(text = text) else msg
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiEngine.close()
    }
}
