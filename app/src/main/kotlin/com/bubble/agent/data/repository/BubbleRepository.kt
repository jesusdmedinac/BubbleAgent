package com.bubble.agent.data.repository

import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.OfflineProfile
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.data.model.VaultCategory
import com.bubble.agent.data.model.VaultMemoryItem
import com.bubble.agent.data.tracker.ScreenTimeTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BubbleRepository {
    val offlineProfile: StateFlow<OfflineProfile>
    val recommendedActivities: StateFlow<List<RecommendedActivity>>
    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot>
    val vaultMemories: StateFlow<List<VaultMemoryItem>>

    fun updateProfile(profile: OfflineProfile)
    fun addInterest(interest: String)
    fun saveRecommendedActivity(
        title: String,
        category: String,
        estimatedMinutes: Int,
        reason: String
    ): RecommendedActivity
    fun toggleActivityCompleted(id: String)
    fun refreshSnapshot()
    fun addVaultMemory(item: VaultMemoryItem)
    fun deleteVaultMemory(id: String)
}

class BubbleRepositoryImpl(
    private val screenTimeTracker: ScreenTimeTracker
) : BubbleRepository {

    private val _offlineProfile = MutableStateFlow(OfflineProfile())
    override val offlineProfile: StateFlow<OfflineProfile> = _offlineProfile.asStateFlow()

    private val _recommendedActivities = MutableStateFlow<List<RecommendedActivity>>(
        listOf(
            RecommendedActivity(
                title = "Aprender acordes básicos de guitarra acústica",
                category = "MUSIC",
                estimatedMinutes = 25,
                reason = "Reemplaza los 50 minutos dedicados a redes sociales por una práctica musical activa."
            ),
            RecommendedActivity(
                title = "Caminata de desconexión sin teléfono",
                category = "NATURE",
                estimatedMinutes = 30,
                reason = "Reduce fatiga cognitiva y resetea los picos de dopamina tras uso continuo de pantalla."
            )
        )
    )
    override val recommendedActivities: StateFlow<List<RecommendedActivity>> =
        _recommendedActivities.asStateFlow()

    private val _digitalHealthSnapshot = MutableStateFlow(
        screenTimeTracker.getTodaySnapshot(_offlineProfile.value.interests)
    )
    override val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> =
        _digitalHealthSnapshot.asStateFlow()

    private val _vaultMemories = MutableStateFlow<List<VaultMemoryItem>>(
        listOf(
            VaultMemoryItem(
                tag = "META OFFLINE",
                category = VaultCategory.OFFLINE_GOAL,
                title = "Sustituir doomscrolling nocturno en Instagram por 25 min de práctica de guitarra acústica (fingerpicking).",
                source = "Hace 2h • Gemini Nano",
                timestampFormatted = "Hace 2h",
                footerDetail = "Origen: Chat local • SQLite Encriptada on-device",
                tagColorHex = 0xFF1BB5C6
            ),
            VaultMemoryItem(
                tag = "HÁBITO DETECTADO",
                category = VaultCategory.MCP_HABIT,
                title = "Pico de 14 desbloqueos compulsivos de Instagram entre 2:00 PM y 3:30 PM sin intención clara.",
                source = "Hoy 3:15 PM • Android MCP",
                timestampFormatted = "Hoy 3:15 PM",
                footerDetail = "Sensor MCP: AppUsageTracker • Sugerir respiración 4-7-8",
                tagColorHex = 0xFFF5A623
            ),
            VaultMemoryItem(
                tag = "PREFERENCIA",
                category = VaultCategory.PREFERENCE,
                title = "Activar automáticamente No Molestar y silenciar apps de ocio durante sesiones de lectura (8:00 - 9:00 PM).",
                source = "Ayer • Context Center",
                timestampFormatted = "Ayer",
                footerDetail = "Regla activa on-device • Expuesta a Gemini Nano",
                tagColorHex = 0xFF5C8DEF
            ),
            VaultMemoryItem(
                tag = "INTERÉS DETECTADO",
                category = VaultCategory.OFFLINE_GOAL,
                title = "Enfocado en aprender fingerpicking y la regla de los 2 minutos de Atomic Habits.",
                source = "30 Oct • Bubble Agent",
                timestampFormatted = "30 Oct",
                footerDetail = "Memoria contextual para priorizar actividades offline",
                tagColorHex = 0xFF10B981
            )
        )
    )
    override val vaultMemories: StateFlow<List<VaultMemoryItem>> = _vaultMemories.asStateFlow()

    override fun addVaultMemory(item: VaultMemoryItem) {
        _vaultMemories.update { current -> listOf(item) + current }
    }

    override fun deleteVaultMemory(id: String) {
        _vaultMemories.update { current -> current.filter { it.id != id } }
    }

    override fun updateProfile(profile: OfflineProfile) {
        _offlineProfile.value = profile
        refreshSnapshot()
    }

    override fun addInterest(interest: String) {
        _offlineProfile.update { current ->
            if (current.interests.contains(interest)) current
            else current.copy(interests = current.interests + interest)
        }
        refreshSnapshot()
    }

    override fun saveRecommendedActivity(
        title: String,
        category: String,
        estimatedMinutes: Int,
        reason: String
    ): RecommendedActivity {
        val newActivity = RecommendedActivity(
            title = title,
            category = category.uppercase(),
            estimatedMinutes = estimatedMinutes,
            reason = reason
        )
        _recommendedActivities.update { current ->
            listOf(newActivity) + current
        }
        return newActivity
    }

    override fun toggleActivityCompleted(id: String) {
        _recommendedActivities.update { current ->
            current.map {
                if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
            }
        }
    }

    override fun refreshSnapshot() {
        _digitalHealthSnapshot.value =
            screenTimeTracker.getTodaySnapshot(_offlineProfile.value.interests)
    }
}
