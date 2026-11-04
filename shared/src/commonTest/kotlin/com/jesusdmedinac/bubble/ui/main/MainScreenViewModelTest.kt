package com.jesusdmedinac.bubble.ui.main

import com.jesusdmedinac.bubble.ai.BubbleAiEngine
import com.jesusdmedinac.bubble.core.currentTimeMillis
import com.jesusdmedinac.bubble.data.model.ChatMessage
import com.jesusdmedinac.bubble.data.model.Conversation
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.MessageSender
import com.jesusdmedinac.bubble.data.model.OfflineProfile
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.data.model.StoreAppItem
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.data.tracker.FakeScreenTimeSource
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class ImmediateAiEngine : BubbleAiEngine {
    override fun streamChat(userPrompt: String): Flow<String> = flow {
        emit("Mock response")
    }
    override fun close() {}
}

class FakeBubbleRepository : BubbleRepository {
    private val _offlineProfile = MutableStateFlow(OfflineProfile())
    override val offlineProfile: StateFlow<OfflineProfile> = _offlineProfile.asStateFlow()

    private val _recommendedActivities = MutableStateFlow<List<RecommendedActivity>>(emptyList())
    override val recommendedActivities: StateFlow<List<RecommendedActivity>> = _recommendedActivities.asStateFlow()

    private val _digitalHealthSnapshot = MutableStateFlow(
        DigitalHealthSnapshot(
            totalScreenTimeMinutes = 240,
            potentialRecoverableMinutes = 90,
            topApps = emptyList(),
            isPermissionGranted = false,
            offlineInterests = listOf("Tocar guitarra acústica")
        )
    )
    override val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> = _digitalHealthSnapshot.asStateFlow()

    override fun updateProfile(profile: OfflineProfile) {
        _offlineProfile.value = profile
    }

    override fun addInterest(interest: String) {
        _offlineProfile.update { it.copy(interests = it.interests + interest) }
    }

    override fun saveRecommendedActivity(
        title: String,
        category: String,
        estimatedMinutes: Int,
        reason: String
    ): RecommendedActivity {
        val act = RecommendedActivity(
            title = title,
            category = category,
            estimatedMinutes = estimatedMinutes,
            reason = reason
        )
        _recommendedActivities.update { listOf(act) + it }
        return act
    }

    override fun toggleActivityCompleted(id: String) {
        _recommendedActivities.update { current ->
            current.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
        }
    }

    private val _vaultMemories = MutableStateFlow<List<VaultMemoryItem>>(emptyList())
    override val vaultMemories: StateFlow<List<VaultMemoryItem>> = _vaultMemories.asStateFlow()

    override fun addVaultMemory(item: VaultMemoryItem) {
        _vaultMemories.update { listOf(item) + it }
    }

    override fun deleteVaultMemory(id: String) {
        _vaultMemories.update { current -> current.filter { it.id != id } }
    }

    override fun refreshSnapshot() {}

    private val _storeApps = MutableStateFlow<List<StoreAppItem>>(emptyList())
    override val storeApps: StateFlow<List<StoreAppItem>> = _storeApps.asStateFlow()

    override fun searchStoreApps(query: String, category: String?): List<StoreAppItem> = emptyList()
    override fun getStoreAppDetails(packageName: String): StoreAppItem? = null
    override fun listMindfulApps(): List<StoreAppItem> = emptyList()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    override fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> = flow {
        emit(_chatMessages.value.filter { it.conversationId == conversationId })
    }

    override fun createOrUpdateConversation(id: String, title: String) {
        _conversations.update { list ->
            val existing = list.find { it.id == id }
            if (existing != null) {
                list.map { if (it.id == id) it.copy(title = title, updatedAt = currentTimeMillis()) else it }
            } else {
                listOf(Conversation(id = id, title = title)) + list
            }
        }
    }

    override fun updateConversationTimestamp(id: String) {
        _conversations.update { list ->
            list.map { if (it.id == id) it.copy(updatedAt = currentTimeMillis()) else it }
        }
    }

    override fun deleteConversation(id: String) {
        _conversations.update { list -> list.filter { it.id != id } }
        _chatMessages.update { list -> list.filter { it.conversationId != id } }
    }

    override fun updateChatMessageSdui(id: String, sduiJson: String?) {
        _chatMessages.update { list ->
            list.map { if (it.id == id) it.copy(sduiJson = sduiJson) else it }
        }
    }

    override fun insertChatMessage(message: ChatMessage) {
        _chatMessages.update { it + message }
    }
    override fun updateChatMessageText(id: String, text: String) {
        _chatMessages.update { list -> list.map { if (it.id == id) it.copy(text = text) else it } }
    }
    override fun clearChatMessages() {
        _chatMessages.value = emptyList()
    }
}

class MainScreenViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUpMainDispatcher() {
        // Run viewModelScope on the test thread so state updates arrive in order.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterTest
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MainScreenViewModel {
        return MainScreenViewModel(
            aiEngine = ImmediateAiEngine(),
            repository = FakeBubbleRepository(),
            screenTimeSource = FakeScreenTimeSource()
        )
    }

    @Test
    fun uiState_initiallyEmptyMessages() = runTest {
        val viewModel = createViewModel()
        val state = viewModel.uiState.first()
        assertTrue(state.messages.isEmpty())
        assertEquals("Jesús Daniel", state.userName)
    }

    @Test
    fun uiState_onInputTextChanged() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Hola Bubble")
        val state = viewModel.uiState.first { it.inputText == "Hola Bubble" }
        assertEquals("Hola Bubble", state.inputText)
    }

    @Test
    fun uiState_onSendMessage_clearsInputAndAddsUserAndAssistantMessage() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Tocar guitarra")
        viewModel.onSendMessage()

        val state = viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating }
        assertEquals("", state.inputText)
        assertEquals(2, state.messages.size)
        assertEquals(MessageSender.USER, state.messages[0].sender)
        assertEquals("Tocar guitarra", state.messages[0].text)
        assertEquals(MessageSender.BUBBLE, state.messages[1].sender)
    }

    @Test
    fun uiState_onNewChatClicked_clearsConversation() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Mensaje 1")
        viewModel.onSendMessage()
        viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating }

        viewModel.onNewChatClicked()

        val state = viewModel.uiState.first { it.messages.isEmpty() && it.inputText.isEmpty() }
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.inputText)
    }

    @Test
    fun uiState_sameConversation_doesNotDuplicateRecentConversation() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Cómo reducir screen time")
        viewModel.onSendMessage()
        viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating }

        // Second message in the same conversation
        viewModel.onInputTextChanged("Aprender piano")
        viewModel.onSendMessage()
        val state = viewModel.uiState.first { it.messages.size >= 4 && !it.isGenerating }

        // Must still be 1 conversation in recents
        assertEquals(1, state.recentConversations.size)
        assertEquals("Cómo reducir screen time", state.recentConversations[0].title)
    }

    @Test
    fun uiState_newConversation_createsSecondRecentConversation() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Cómo reducir screen time")
        viewModel.onSendMessage()
        viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating }

        // Start a new chat
        viewModel.onNewChatClicked()
        viewModel.onInputTextChanged("Aprender piano")
        viewModel.onSendMessage()
        val state = viewModel.uiState.first { it.recentConversations.size == 2 && !it.isGenerating }

        assertEquals(2, state.recentConversations.size)
        assertEquals("Aprender piano", state.recentConversations[0].title)
        assertEquals("Cómo reducir screen time", state.recentConversations[1].title)
    }

    @Test
    fun uiState_onSelectConversation_switchesActiveMessages() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Conversación 1")
        viewModel.onSendMessage()
        val firstState = viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating }
        val firstConvId = firstState.currentConversationId

        viewModel.onNewChatClicked()
        viewModel.onInputTextChanged("Conversación 2")
        viewModel.onSendMessage()
        val secondState = viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating && it.currentConversationId != firstConvId }
        val secondConvId = secondState.currentConversationId

        // Switch back to conversation 1 without sending any message
        viewModel.onSelectConversation(firstConvId)
        val switchedState = viewModel.uiState.first { it.currentConversationId == firstConvId }
        assertEquals("Conversación 1", switchedState.messages.first { it.sender == MessageSender.USER }.text)
    }

    @Test
    fun sdui_vaultMemoryCandidate_detectedAndConfirmed() = runTest {
        val repo = FakeBubbleRepository()
        val viewModel = MainScreenViewModel(
            aiEngine = ImmediateAiEngine(),
            repository = repo,
            screenTimeSource = FakeScreenTimeSource()
        )
        viewModel.onInputTextChanged("Quiero aprender guitarra acústica hoy")
        viewModel.onSendMessage()

        val state = viewModel.uiState.first { it.messages.size >= 2 && !it.isGenerating && it.messages.any { msg -> msg.sduiJson != null } }
        val assistantMsg = state.messages.first { it.sender == MessageSender.BUBBLE }
        assertTrue(assistantMsg.sduiJson != null)
        assertTrue(assistantMsg.sduiJson!!.contains("confirm_vault"))

        // Simulate user confirming via json-to-compose custom action
        viewModel.onCustomAction(
            com.jesusdmedinac.jsontocompose.model.ComposeAction.Custom(
                customType = com.jesusdmedinac.bubble.ui.main.sdui.BubbleSduiTemplates.ACTION_CONFIRM_VAULT,
                params = kotlinx.serialization.json.buildJsonObject {
                    put("title", kotlinx.serialization.json.JsonPrimitive("Interés en práctica musical (guitarra acústica)"))
                    put("tag", kotlinx.serialization.json.JsonPrimitive("MÚSICA"))
                    put("category", kotlinx.serialization.json.JsonPrimitive("PREFERENCE"))
                }
            ),
            assistantMsg.id
        )

        // Verify it was persisted to repo.vaultMemories
        val memories = repo.vaultMemories.first()
        assertEquals(1, memories.size)
        assertEquals("Interés en práctica musical (guitarra acústica)", memories[0].title)

        // Verify message SDUI was updated with success card
        val updatedState = viewModel.uiState.first { s ->
            s.messages.firstOrNull { it.id == assistantMsg.id }?.sduiJson?.contains("Guardado en tu Vault") == true
        }
        val updatedAssistantMsg = updatedState.messages.first { it.id == assistantMsg.id }
        assertTrue(updatedAssistantMsg.sduiJson!!.contains("Guardado en tu Vault"))
    }

    @Test
    fun repository_simulateGeminiMcpInvocation_savesActivity() = runTest {
        val viewModel = createViewModel()
        viewModel.simulateGeminiMcpInvocation()

        val activities = viewModel.recommendedActivities.first()
        assertEquals(1, activities.size)
        assertTrue(activities[0].reason.contains("Gemini MCP"))
    }
}
