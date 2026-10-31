package com.bubble.agent.ui.main

import com.bubble.agent.ai.MockAiEngine
import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.OfflineProfile
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.data.model.VaultMemoryItem
import com.bubble.agent.data.repository.BubbleRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Test

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
}

class MainScreenViewModelTest {

    private fun createViewModel(): MainScreenViewModel {
        return MainScreenViewModel(
            aiEngine = MockAiEngine(),
            repository = FakeBubbleRepository()
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
        assertEquals("Hola Bubble", viewModel.uiState.first().inputText)
    }

    @Test
    fun uiState_onSendMessage_clearsInputAndAddsUserAndAssistantMessage() = runTest {
        val viewModel = createViewModel()
        viewModel.onInputTextChanged("Tocar guitarra")
        viewModel.onSendMessage()

        val state = viewModel.uiState.first()
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
        viewModel.onNewChatClicked()

        val state = viewModel.uiState.first()
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.inputText)
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
