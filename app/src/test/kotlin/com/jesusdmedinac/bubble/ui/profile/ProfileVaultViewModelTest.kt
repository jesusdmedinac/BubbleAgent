package com.jesusdmedinac.bubble.ui.profile

import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.OfflineProfile
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.data.model.StoreAppItem
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeProfileBubbleRepository : BubbleRepository {
    private val _offlineProfile = MutableStateFlow(OfflineProfile())
    override val offlineProfile: StateFlow<OfflineProfile> = _offlineProfile.asStateFlow()

    private val _recommendedActivities = MutableStateFlow<List<RecommendedActivity>>(emptyList())
    override val recommendedActivities: StateFlow<List<RecommendedActivity>> = _recommendedActivities.asStateFlow()

    private val _digitalHealthSnapshot = MutableStateFlow(
        DigitalHealthSnapshot(
            totalScreenTimeMinutes = 134, // 2h 14m
            potentialRecoverableMinutes = 90,
            topApps = emptyList(),
            isPermissionGranted = true,
            offlineInterests = listOf("Tocar guitarra acústica")
        )
    )
    override val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> = _digitalHealthSnapshot.asStateFlow()

    private val _vaultMemories = MutableStateFlow(
        listOf(
            VaultMemoryItem(
                tag = "META OFFLINE",
                category = VaultCategory.OFFLINE_GOAL,
                title = "Practicar guitarra acústica",
                source = "Gemini Nano",
                timestampFormatted = "Hace 2h",
                footerDetail = "SQLite"
            ),
            VaultMemoryItem(
                tag = "HÁBITO DETECTADO",
                category = VaultCategory.MCP_HABIT,
                title = "14 desbloqueos compulsivos",
                source = "Android MCP",
                timestampFormatted = "Hoy 3:15 PM",
                footerDetail = "AppUsageTracker"
            ),
            VaultMemoryItem(
                tag = "PREFERENCIA",
                category = VaultCategory.PREFERENCE,
                title = "Silenciar notificaciones",
                source = "Context Center",
                timestampFormatted = "Ayer",
                footerDetail = "DND"
            )
        )
    )
    override val vaultMemories: StateFlow<List<VaultMemoryItem>> = _vaultMemories.asStateFlow()

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
        val act = RecommendedActivity(title = title, category = category, estimatedMinutes = estimatedMinutes, reason = reason)
        _recommendedActivities.update { listOf(act) + it }
        return act
    }

    var refreshSnapshotCallCount = 0

    fun setSnapshot(snapshot: DigitalHealthSnapshot) {
        _digitalHealthSnapshot.value = snapshot
    }

    override fun toggleActivityCompleted(id: String) {}
    override fun refreshSnapshot() {
        refreshSnapshotCallCount++
    }
    override fun addVaultMemory(item: VaultMemoryItem) {
        _vaultMemories.update { listOf(item) + it }
    }
    override fun deleteVaultMemory(id: String) {
        _vaultMemories.update { current -> current.filter { it.id != id } }
    }

    private val _storeApps = MutableStateFlow<List<StoreAppItem>>(emptyList())
    override val storeApps: StateFlow<List<StoreAppItem>> = _storeApps.asStateFlow()

    override fun searchStoreApps(query: String, category: String?): List<StoreAppItem> = emptyList()
    override fun getStoreAppDetails(packageName: String): StoreAppItem? = null
    override fun listMindfulApps(): List<StoreAppItem> = emptyList()

    private val _chatMessages = MutableStateFlow<List<com.jesusdmedinac.bubble.data.model.ChatMessage>>(emptyList())
    override val chatMessages: StateFlow<List<com.jesusdmedinac.bubble.data.model.ChatMessage>> = _chatMessages.asStateFlow()

    override fun insertChatMessage(message: com.jesusdmedinac.bubble.data.model.ChatMessage) {
        _chatMessages.update { it + message }
    }
    override fun updateChatMessageText(id: String, text: String) {
        _chatMessages.update { list -> list.map { if (it.id == id) it.copy(text = text) else it } }
    }
    override fun clearChatMessages() {
        _chatMessages.value = emptyList()
    }
}

class ProfileVaultViewModelTest {

    @Test
    fun initialState_loadsScreenTimeAndMemories() = runTest {
        val repository = FakeProfileBubbleRepository()
        val viewModel = ProfileVaultViewModel(repository)

        val state = viewModel.uiState.first { it.displayedMemories.isNotEmpty() }

        assertEquals("Jesús Daniel Medina", state.userName)
        assertEquals(2L, state.screenTimeHours)
        assertEquals(14L, state.screenTimeMinutes)
        assertEquals(3, state.displayedMemories.size)
        assertTrue(state.isUsagePermissionGranted)
    }

    @Test
    fun filterByCategory_updatesDisplayedMemories() = runTest {
        val repository = FakeProfileBubbleRepository()
        val viewModel = ProfileVaultViewModel(repository)

        viewModel.onCategorySelected(VaultCategory.OFFLINE_GOAL)

        val state = viewModel.uiState.first { it.selectedCategory == VaultCategory.OFFLINE_GOAL }
        assertEquals(1, state.displayedMemories.size)
        assertEquals("META OFFLINE", state.displayedMemories.first().tag)
    }

    @Test
    fun permissionNotGranted_exposesFalseFlagAndDemoValues() = runTest {
        val repository = FakeProfileBubbleRepository()
        repository.setSnapshot(
            DigitalHealthSnapshot(
                totalScreenTimeMinutes = 255L,
                potentialRecoverableMinutes = 120L,
                topApps = emptyList(),
                isPermissionGranted = false
            )
        )
        val viewModel = ProfileVaultViewModel(repository)

        val state = viewModel.uiState.first { it.displayedMemories.isNotEmpty() && !it.isUsagePermissionGranted }

        assertEquals(false, state.isUsagePermissionGranted)
        assertEquals(4L, state.screenTimeHours)
        assertEquals(15L, state.screenTimeMinutes)
        assertEquals("-38% vs ayer", state.screenTimeTrend)
    }

    @Test
    fun refreshPermissionAndStats_triggersRepositoryRefresh() = runTest {
        val repository = FakeProfileBubbleRepository()
        val viewModel = ProfileVaultViewModel(repository)

        viewModel.refreshPermissionAndStats()

        assertEquals(1, repository.refreshSnapshotCallCount)
    }
}
