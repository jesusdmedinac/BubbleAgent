package com.bubble.agent.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubble.agent.BubbleApplication
import com.bubble.agent.data.model.AppUsageStat
import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.VaultCategory
import com.bubble.agent.data.model.VaultMemoryItem
import com.bubble.agent.data.repository.BubbleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileVaultUiState(
    val userName: String = "Jesús Daniel Medina",
    val userHandle: String = "@jesusdmedinac • Pro Account",
    val focusStreakDays: Int = 7,
    val totalMemoriesCount: Int = 48,
    val selectedCategory: VaultCategory = VaultCategory.ALL,
    val screenTimeHours: Long = 2,
    val screenTimeMinutes: Long = 14,
    val screenTimeTrend: String = "-38% vs ayer",
    val offlineGainText: String = "+1h 45m de vida offline ganada (Guitarra & Lectura)",
    val topApps: List<AppUsageStat> = emptyList(),
    val displayedMemories: List<VaultMemoryItem> = emptyList()
)

class ProfileVaultViewModel(
    private val repository: BubbleRepository = BubbleApplication.instance.appContainer.bubbleRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow(VaultCategory.ALL)

    val uiState: StateFlow<ProfileVaultUiState> = combine(
        repository.digitalHealthSnapshot,
        repository.vaultMemories,
        _selectedCategory
    ) { snapshot, memories, category ->
        val filtered = when (category) {
            VaultCategory.ALL -> memories
            else -> memories.filter { it.category == category }
        }

        // Format screen time from real usage if available
        val totalMinutes = snapshot.totalScreenTimeMinutes
        val hours = if (totalMinutes > 0) totalMinutes / 60 else 2L
        val mins = if (totalMinutes > 0) totalMinutes % 60 else 14L

        val apps = if (snapshot.topApps.isNotEmpty()) {
            snapshot.topApps
        } else {
            listOf(
                AppUsageStat("Instagram", "com.instagram.android", 42L),
                AppUsageStat("YouTube", "com.google.android.youtube", 28L),
                AppUsageStat("X (Twitter)", "com.twitter.android", 18L)
            )
        }

        ProfileVaultUiState(
            userName = "Jesús Daniel Medina",
            userHandle = "@jesusdmedinac • Pro Account",
            focusStreakDays = 7,
            totalMemoriesCount = 48,
            selectedCategory = category,
            screenTimeHours = hours,
            screenTimeMinutes = mins,
            screenTimeTrend = "-38% vs ayer",
            offlineGainText = "+1h 45m de vida offline ganada (Guitarra & Lectura)",
            topApps = apps,
            displayedMemories = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileVaultUiState()
    )

    fun onCategorySelected(category: VaultCategory) {
        _selectedCategory.value = category
    }
}
