package com.jesusdmedinac.bubble.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jesusdmedinac.bubble.data.model.AppUsageStat
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileVaultUiState(
    val userName: String = "Jesús Daniel Medina",
    val userHandle: String = "@jesusdmedinac • Pro Account",
    val focusStreakDays: Int = 7,
    val totalMemoriesCount: Int = 0,
    val selectedCategory: VaultCategory = VaultCategory.ALL,
    val screenTimeHours: Long = 0,
    val screenTimeMinutes: Long = 0,
    val screenTimeTrend: String = "",
    val offlineGainText: String = "",
    val topApps: List<AppUsageStat> = emptyList(),
    val displayedMemories: List<VaultMemoryItem> = emptyList(),
    val isUsagePermissionGranted: Boolean = false
)

class ProfileVaultViewModel(
    private val repository: BubbleRepository,
    private val screenTimeSource: ScreenTimeSource,
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
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60

        val apps = if (snapshot.topApps.isNotEmpty()) {
            snapshot.topApps
        } else if (!snapshot.isPermissionGranted) {
            listOf(
                AppUsageStat("Instagram", "com.instagram.android", 105L),
                AppUsageStat("TikTok", "com.zhiliaoapp.musically", 70L),
                AppUsageStat("X / Twitter", "com.twitter.android", 50L),
                AppUsageStat("YouTube", "com.google.android.youtube", 30L)
            )
        } else {
            emptyList()
        }

        val offlineGain = if (snapshot.potentialRecoverableMinutes > 0) {
            val recHours = snapshot.potentialRecoverableMinutes / 60
            val recMins = snapshot.potentialRecoverableMinutes % 60
            if (recHours > 0) {
                "+${recHours}h ${recMins}m de vida offline ganada (Guitarra & Lectura)"
            } else {
                "+${recMins}m de vida offline ganada (Guitarra & Lectura)"
            }
        } else {
            "+1h 45m de vida offline ganada (Guitarra & Lectura)"
        }

        val trend = if (snapshot.isPermissionGranted) {
            "Sincronizado"
        } else {
            "-38% vs ayer"
        }

        ProfileVaultUiState(
            userName = "Jesús Daniel Medina",
            userHandle = "@jesusdmedinac • Pro Account",
            focusStreakDays = 7,
            totalMemoriesCount = memories.size,
            selectedCategory = category,
            screenTimeHours = hours,
            screenTimeMinutes = mins,
            screenTimeTrend = trend,
            offlineGainText = offlineGain,
            topApps = apps,
            displayedMemories = filtered,
            isUsagePermissionGranted = snapshot.isPermissionGranted
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileVaultUiState()
    )

    fun onCategorySelected(category: VaultCategory) {
        _selectedCategory.value = category
    }

    fun refreshPermissionAndStats() {
        repository.refreshSnapshot()
    }

    fun openUsageSettings() {
        screenTimeSource.openUsageAccessSettings()
    }
}
