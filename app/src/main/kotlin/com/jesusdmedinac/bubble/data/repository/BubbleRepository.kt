package com.jesusdmedinac.bubble.data.repository

import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.OfflineProfile
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.data.model.StoreAppItem
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BubbleRepository {
    val offlineProfile: StateFlow<OfflineProfile>
    val recommendedActivities: StateFlow<List<RecommendedActivity>>
    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot>
    val vaultMemories: StateFlow<List<VaultMemoryItem>>
    val storeApps: StateFlow<List<StoreAppItem>>

    fun searchStoreApps(query: String, category: String? = null): List<StoreAppItem>
    fun getStoreAppDetails(packageName: String): StoreAppItem?
    fun listMindfulApps(): List<StoreAppItem>

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

    private val _storeApps = MutableStateFlow<List<StoreAppItem>>(
        listOf(
            StoreAppItem(
                packageName = "com.bubble.guitar",
                name = "Guitar Mindful Practice",
                tagline = "Afinador y acordes minimalistas sin anuncios.",
                description = "Práctica activa de guitarra acústica para reemplazar momentos de ocio pasivo y sobrestimulación de pantalla.",
                category = "MUSIC",
                isMindful = true,
                rating = 4.9f
            ),
            StoreAppItem(
                packageName = "com.bubble.breath",
                name = "Breathe 4-7-8 Focus",
                tagline = "Respiración consciente y micro-pausas.",
                description = "Guía háptica de respiración para calmar el impulso de desbloquear el teléfono sin motivo.",
                category = "HEALTH",
                isMindful = true,
                rating = 4.8f
            ),
            StoreAppItem(
                packageName = "com.jesusdmedinac.bubble",
                name = "Bubble Minimal Phone",
                tagline = "Launcher minimalista centrado en lo esencial.",
                description = "Tu teléfono libre de feeds infinitos y notificaciones tóxicas. Acceso directo a llamadas, notas y apps mindful.",
                category = "UTILITY",
                isMindful = true,
                rating = 5.0f,
                isInstalled = true
            ),
            StoreAppItem(
                packageName = "com.bubble.reader",
                name = "Offscreen Companion",
                tagline = "Seguimiento de libros físicos y notas analógicas.",
                description = "Registra tus páginas leídas en libros de papel, notas marginales y metas de lectura sin pantallas brillantes.",
                category = "EDUCATION",
                isMindful = true,
                rating = 4.7f
            ),
            StoreAppItem(
                packageName = "com.bubble.zen",
                name = "Zen Sketch & Ink",
                tagline = "Lienzo de dibujo táctil para liberar la mente.",
                description = "Dibuja libremente con stylus o tus dedos. Cero telemetría, cero funciones sociales adictivas.",
                category = "CREATIVITY",
                isMindful = true,
                rating = 4.9f
            )
        )
    )
    override val storeApps: StateFlow<List<StoreAppItem>> = _storeApps.asStateFlow()

    override fun searchStoreApps(query: String, category: String?): List<StoreAppItem> {
        val q = query.trim().lowercase()
        val cat = category?.trim()?.uppercase()
        return _storeApps.value.filter { app ->
            val matchesQuery = q.isEmpty() ||
                app.name.lowercase().contains(q) ||
                app.tagline.lowercase().contains(q) ||
                app.description.lowercase().contains(q) ||
                app.packageName.lowercase().contains(q)
            val matchesCat = cat.isNullOrEmpty() || app.category.uppercase() == cat
            matchesQuery && matchesCat
        }
    }

    override fun getStoreAppDetails(packageName: String): StoreAppItem? {
        return _storeApps.value.find { it.packageName.equals(packageName, ignoreCase = true) }
    }

    override fun listMindfulApps(): List<StoreAppItem> {
        return _storeApps.value.filter { it.isMindful }
    }
}
