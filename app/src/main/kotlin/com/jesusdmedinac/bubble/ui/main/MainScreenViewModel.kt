package com.jesusdmedinac.bubble.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesusdmedinac.bubble.BubbleApplication
import com.jesusdmedinac.bubble.ai.BubbleAiEngine
import com.jesusdmedinac.bubble.ai.MockAiEngine
import com.jesusdmedinac.bubble.data.model.ChatMessage
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.MessageSender
import com.jesusdmedinac.bubble.data.model.OfflineProfile
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

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

    private val _inputText = MutableStateFlow("")
    private val _isGenerating = MutableStateFlow(false)
    private val _showDashboardHeader = MutableStateFlow(true)

    val uiState: StateFlow<ChatUiState> = combine(
        repository.chatMessages,
        _inputText,
        _isGenerating,
        _showDashboardHeader
    ) { messages, inputText, isGenerating, showDashboardHeader ->
        ChatUiState(
            messages = messages,
            inputText = inputText,
            isGenerating = isGenerating,
            selectedTab = 2,
            userName = "Jesús Daniel",
            showDashboardHeader = showDashboardHeader
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ChatUiState())

    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> =
        repository.digitalHealthSnapshot

    val recommendedActivities: StateFlow<List<RecommendedActivity>> =
        repository.recommendedActivities

    val offlineProfile: StateFlow<OfflineProfile> =
        repository.offlineProfile

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun onNewChatClicked() {
        repository.clearChatMessages()
        _inputText.value = ""
    }

    fun toggleDashboardHeader() {
        _showDashboardHeader.update { !it }
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
        } catch (_: Exception) {
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
        val query = _inputText.value.trim()
        if (query.isEmpty() || _isGenerating.value) return

        val userMessage = ChatMessage(
            sender = MessageSender.USER,
            text = query
        )
        repository.insertChatMessage(userMessage)

        val assistantMsgId = UUID.randomUUID().toString()
        val initialAssistantMessage = ChatMessage(
            id = assistantMsgId,
            sender = MessageSender.BUBBLE,
            text = ""
        )
        repository.insertChatMessage(initialAssistantMessage)

        _inputText.value = ""
        _isGenerating.value = true

        viewModelScope.launch {
            var accumulated = ""
            aiEngine.streamChat(query)
                .catch {
                    if (accumulated.isEmpty()) {
                        accumulated = "¡Hola Jesús! Estoy aquí en tu dispositivo de forma 100% privada y local vía Gemini Nano. ¿Qué actividad del mundo real te gustaría priorizar hoy?"
                        repository.updateChatMessageText(assistantMsgId, accumulated)
                    }
                }
                .collect { token ->
                    accumulated += token
                    repository.updateChatMessageText(assistantMsgId, accumulated)
                }

            _isGenerating.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiEngine.close()
    }
}
