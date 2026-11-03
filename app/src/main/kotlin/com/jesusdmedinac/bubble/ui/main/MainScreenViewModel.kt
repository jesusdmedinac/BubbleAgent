package com.jesusdmedinac.bubble.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesusdmedinac.bubble.BubbleApplication
import com.jesusdmedinac.bubble.ai.BubbleAiEngine
import com.jesusdmedinac.bubble.ai.MockAiEngine
import com.jesusdmedinac.bubble.ai.vault.VaultMemoryDetector
import com.jesusdmedinac.bubble.data.model.ChatMessage
import com.jesusdmedinac.bubble.data.model.Conversation
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.MessageSender
import com.jesusdmedinac.bubble.data.model.OfflineProfile
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.ui.main.sdui.BubbleSduiTemplates
import com.jesusdmedinac.jsontocompose.model.ComposeAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import java.util.UUID

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val recentQueries: List<String> = emptyList(),
    val recentConversations: List<Conversation> = emptyList(),
    val currentConversationId: String = "",
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

    private val _currentConversationId = MutableStateFlow("")
    private val _inputText = MutableStateFlow("")
    private val _isGenerating = MutableStateFlow(false)
    private val _showDashboardHeader = MutableStateFlow(true)

    private data class ChatInputControlState(
        val currentConvId: String = "",
        val inputText: String = "",
        val isGenerating: Boolean = false,
        val showDashboardHeader: Boolean = true
    )

    private val _controlState = combine(
        _currentConversationId,
        _inputText,
        _isGenerating,
        _showDashboardHeader
    ) { currentConvId, inputText, isGenerating, showDashboardHeader ->
        ChatInputControlState(currentConvId, inputText, isGenerating, showDashboardHeader)
    }

    val uiState: StateFlow<ChatUiState> = combine(
        repository.conversations,
        repository.chatMessages,
        _controlState
    ) { conversations, allMessages, controlState ->
        val activeConvId = if (controlState.currentConvId.isNotEmpty()) {
            controlState.currentConvId
        } else if (conversations.isNotEmpty()) {
            conversations.first().id
        } else {
            ""
        }

        val filteredMessages = if (activeConvId.isNotEmpty()) {
            allMessages.filter { it.conversationId == activeConvId }
        } else {
            allMessages
        }

        val recents = conversations.map { it.title }.take(10)

        ChatUiState(
            messages = filteredMessages,
            recentQueries = recents,
            recentConversations = conversations.take(10),
            currentConversationId = activeConvId,
            inputText = controlState.inputText,
            isGenerating = controlState.isGenerating,
            selectedTab = 2,
            userName = "Jesús Daniel",
            showDashboardHeader = controlState.showDashboardHeader
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

    fun onSelectConversation(conversationId: String) {
        _currentConversationId.value = conversationId
    }

    fun onSelectConversationByTitle(title: String) {
        val found = repository.conversations.value.firstOrNull { it.title.equals(title, ignoreCase = true) }
        if (found != null) {
            _currentConversationId.value = found.id
        }
    }

    fun onNewChatClicked() {
        val newId = UUID.randomUUID().toString()
        _currentConversationId.value = newId
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

        var activeConvId = _currentConversationId.value
        if (activeConvId.isEmpty()) {
            activeConvId = UUID.randomUUID().toString()
            _currentConversationId.value = activeConvId
        }

        val existingConv = repository.conversations.value.firstOrNull { it.id == activeConvId }
        if (existingConv == null) {
            val title = if (query.length > 36) query.take(36) + "..." else query
            repository.createOrUpdateConversation(activeConvId, title)
        } else {
            repository.updateConversationTimestamp(activeConvId)
        }

        val userMessage = ChatMessage(
            conversationId = activeConvId,
            sender = MessageSender.USER,
            text = query
        )
        repository.insertChatMessage(userMessage)

        val assistantMsgId = UUID.randomUUID().toString()
        val initialAssistantMessage = ChatMessage(
            id = assistantMsgId,
            conversationId = activeConvId,
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

            // Dynamic Vault candidate detection & SDUI card injection via json-to-compose
            val candidate = VaultMemoryDetector.detectCandidate(query, accumulated)
            if (candidate != null) {
                val sduiDoc = BubbleSduiTemplates.buildConfirmVaultDocument(
                    memoryId = candidate.id,
                    tag = candidate.tag,
                    category = candidate.category,
                    memoryText = candidate.title,
                    source = candidate.source,
                    footerDetail = candidate.footerDetail,
                    tagColorHex = candidate.tagColorHex
                )
                val sduiJson = BubbleSduiTemplates.toJson(sduiDoc)
                repository.updateChatMessageSdui(assistantMsgId, sduiJson)
            }

            _isGenerating.value = false
        }
    }

    fun onCustomAction(action: ComposeAction.Custom, messageId: String) {
        when (action.customType) {
            BubbleSduiTemplates.ACTION_CONFIRM_VAULT -> {
                val title = action.params.get("title")?.let {
                    if (it is JsonPrimitive) it.content else it.toString().trim('"')
                } ?: "Preferencia personal"
                val tag = action.params.get("tag")?.let {
                    if (it is JsonPrimitive) it.content else it.toString().trim('"')
                } ?: "VAULT"
                val categoryStr = action.params.get("category")?.let {
                    if (it is JsonPrimitive) it.content else it.toString().trim('"')
                } ?: "PREFERENCE"
                val source = action.params.get("source")?.let {
                    if (it is JsonPrimitive) it.content else it.toString().trim('"')
                } ?: "Conversación en Bubble"
                val footerDetail = action.params.get("footerDetail")?.let {
                    if (it is JsonPrimitive) it.content else it.toString().trim('"')
                } ?: "Persistido en Vault"
                val tagColorHex = action.params.get("tagColorHex")?.let {
                    val raw = if (it is JsonPrimitive) it.content else it.toString().trim('"')
                    raw.toLongOrNull() ?: 0xFF00D2D3
                } ?: 0xFF00D2D3

                val category = try {
                    VaultCategory.valueOf(categoryStr)
                } catch (_: Exception) {
                    VaultCategory.PREFERENCE
                }

                val memoryItem = VaultMemoryItem(
                    id = UUID.randomUUID().toString(),
                    tag = tag,
                    category = category,
                    title = title,
                    source = source,
                    timestampFormatted = "Hoy",
                    footerDetail = footerDetail,
                    tagColorHex = tagColorHex
                )
                repository.addVaultMemory(memoryItem)
                repository.addInterest(title)

                // Replace confirmation card with confirmed saved card
                val savedDoc = BubbleSduiTemplates.buildMemorySavedDocument(title)
                val savedJson = BubbleSduiTemplates.toJson(savedDoc)
                repository.updateChatMessageSdui(messageId, savedJson)
            }
            BubbleSduiTemplates.ACTION_DISMISS_VAULT -> {
                repository.updateChatMessageSdui(messageId, null)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        aiEngine.close()
    }
}
