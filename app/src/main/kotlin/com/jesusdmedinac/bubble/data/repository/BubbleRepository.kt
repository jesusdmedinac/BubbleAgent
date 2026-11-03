package com.jesusdmedinac.bubble.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.jesusdmedinac.bubble.core.currentTimeMillis
import com.jesusdmedinac.bubble.data.model.ChatMessage
import com.jesusdmedinac.bubble.data.model.Conversation
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.data.model.MessageSender
import com.jesusdmedinac.bubble.data.model.OfflineProfile
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.data.model.StoreAppItem
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeSource
import com.jesusdmedinac.bubble.db.BubbleDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface BubbleRepository {
    val offlineProfile: StateFlow<OfflineProfile>
    val recommendedActivities: StateFlow<List<RecommendedActivity>>
    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot>
    val vaultMemories: StateFlow<List<VaultMemoryItem>>
    val conversations: StateFlow<List<Conversation>>
    val chatMessages: StateFlow<List<ChatMessage>>
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

    // Local Chat & Multi-Conversation Persistence
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>>
    fun createOrUpdateConversation(id: String, title: String)
    fun updateConversationTimestamp(id: String)
    fun deleteConversation(id: String)
    fun insertChatMessage(message: ChatMessage)
    fun updateChatMessageText(id: String, text: String)
    fun updateChatMessageSdui(id: String, sduiJson: String?)
    fun clearChatMessages()
}

class BubbleRepositoryImpl(
    private val screenTimeSource: ScreenTimeSource,
    private val database: BubbleDatabase? = null,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : BubbleRepository {

    private val _offlineProfile = MutableStateFlow(loadInitialProfile())
    override val offlineProfile: StateFlow<OfflineProfile> = _offlineProfile.asStateFlow()

    private val _digitalHealthSnapshot = MutableStateFlow(
        screenTimeSource.getTodaySnapshot(_offlineProfile.value.interests)
    )
    override val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> =
        _digitalHealthSnapshot.asStateFlow()

    // SQLDelight Reactive Vault Memories (No seed data, empty by default)
    private val _inMemoryVault = MutableStateFlow<List<VaultMemoryItem>>(emptyList())
    override val vaultMemories: StateFlow<List<VaultMemoryItem>> = if (database != null) {
        database.bubbleDatabaseQueries
            .selectAllVaultMemories()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val cat = try {
                        VaultCategory.valueOf(entity.category)
                    } catch (_: Exception) {
                        VaultCategory.PREFERENCE
                    }
                    VaultMemoryItem(
                        id = entity.id,
                        tag = entity.tag,
                        category = cat,
                        title = entity.title,
                        source = entity.source,
                        timestampFormatted = entity.timestampFormatted,
                        footerDetail = entity.footerDetail,
                        tagColorHex = entity.tagColorHex
                    )
                }
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
    } else {
        _inMemoryVault.asStateFlow()
    }

    // SQLDelight Reactive Activities (No seed data, empty by default)
    private val _inMemoryActivities = MutableStateFlow<List<RecommendedActivity>>(emptyList())
    override val recommendedActivities: StateFlow<List<RecommendedActivity>> = if (database != null) {
        database.bubbleDatabaseQueries
            .selectAllActivities()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    RecommendedActivity(
                        id = entity.id,
                        title = entity.title,
                        category = entity.category,
                        estimatedMinutes = entity.estimatedMinutes.toInt(),
                        reason = entity.reason,
                        isCompleted = entity.isCompleted != 0L
                    )
                }
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
    } else {
        _inMemoryActivities.asStateFlow()
    }

    // SQLDelight Reactive Conversations
    private val _inMemoryConversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val conversations: StateFlow<List<Conversation>> = if (database != null) {
        database.bubbleDatabaseQueries
            .selectAllConversations()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    Conversation(
                        id = entity.id,
                        title = entity.title,
                        createdAt = entity.createdAt,
                        updatedAt = entity.updatedAt
                    )
                }
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
    } else {
        _inMemoryConversations.asStateFlow()
    }

    // SQLDelight Reactive Chat Messages (No seed data, empty by default)
    private val _inMemoryChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val chatMessages: StateFlow<List<ChatMessage>> = if (database != null) {
        database.bubbleDatabaseQueries
            .selectAllMessages()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    val sender = try {
                        MessageSender.valueOf(entity.sender)
                    } catch (_: Exception) {
                        MessageSender.BUBBLE
                    }
                    ChatMessage(
                        id = entity.id,
                        conversationId = entity.conversationId,
                        sender = sender,
                        text = entity.text,
                        timestamp = entity.timestamp,
                        sduiJson = entity.sduiJson
                    )
                }
            }
            .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())
    } else {
        _inMemoryChat.asStateFlow()
    }

    private fun loadInitialProfile(): OfflineProfile {
        if (database == null) return OfflineProfile()
        return try {
            val entity = database.bubbleDatabaseQueries.getProfile().executeAsOneOrNull()
            if (entity != null) {
                val interests = entity.interestsJson.split("|||").filter { it.isNotBlank() }
                val goals = entity.goalsJson.split("|||").filter { it.isNotBlank() }
                OfflineProfile(
                    interests = interests,
                    idealDurationMinutes = entity.idealDurationMinutes.toInt(),
                    energyLevel = entity.energyLevel,
                    goals = goals
                )
            } else {
                OfflineProfile()
            }
        } catch (_: Exception) {
            OfflineProfile()
        }
    }

    override fun updateProfile(profile: OfflineProfile) {
        _offlineProfile.value = profile
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.upsertProfile(
                    interestsJson = profile.interests.joinToString("|||"),
                    goalsJson = profile.goals.joinToString("|||"),
                    idealDurationMinutes = profile.idealDurationMinutes.toLong(),
                    energyLevel = profile.energyLevel
                )
            }
        }
        refreshSnapshot()
    }

    override fun addInterest(interest: String) {
        val current = _offlineProfile.value
        if (!current.interests.contains(interest)) {
            val updated = current.copy(interests = current.interests + interest)
            updateProfile(updated)
        }
    }

    override fun addVaultMemory(item: VaultMemoryItem) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.insertVaultMemory(
                    id = item.id,
                    tag = item.tag,
                    category = item.category.name,
                    title = item.title,
                    source = item.source,
                    timestampFormatted = item.timestampFormatted,
                    footerDetail = item.footerDetail,
                    tagColorHex = item.tagColorHex,
                    createdAt = currentTimeMillis()
                )
            }
        } else {
            _inMemoryVault.update { current -> listOf(item) + current }
        }
    }

    override fun deleteVaultMemory(id: String) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.deleteVaultMemory(id)
            }
        } else {
            _inMemoryVault.update { current -> current.filter { it.id != id } }
        }
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
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.insertActivity(
                    id = newActivity.id,
                    title = newActivity.title,
                    category = newActivity.category,
                    estimatedMinutes = newActivity.estimatedMinutes.toLong(),
                    reason = newActivity.reason,
                    isCompleted = if (newActivity.isCompleted) 1L else 0L,
                    createdAt = currentTimeMillis()
                )
            }
        } else {
            _inMemoryActivities.update { current -> listOf(newActivity) + current }
        }
        return newActivity
    }

    override fun toggleActivityCompleted(id: String) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.toggleActivityCompleted(id)
            }
        } else {
            _inMemoryActivities.update { current ->
                current.map {
                    if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
                }
            }
        }
    }

    override fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessage>> {
        return if (database != null) {
            database.bubbleDatabaseQueries
                .selectMessagesByConversation(conversationId)
                .asFlow()
                .mapToList(Dispatchers.IO)
                .map { entities ->
                    entities.map { entity ->
                        val sender = try {
                            MessageSender.valueOf(entity.sender)
                        } catch (_: Exception) {
                            MessageSender.BUBBLE
                        }
                        ChatMessage(
                            id = entity.id,
                            conversationId = entity.conversationId,
                            sender = sender,
                            text = entity.text,
                            timestamp = entity.timestamp,
                            sduiJson = entity.sduiJson
                        )
                    }
                }
        } else {
            _inMemoryChat.map { list -> list.filter { it.conversationId == conversationId } }
        }
    }

    override fun createOrUpdateConversation(id: String, title: String) {
        val now = currentTimeMillis()
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.insertConversation(
                    id = id,
                    title = title,
                    createdAt = now,
                    updatedAt = now
                )
            }
        } else {
            _inMemoryConversations.update { list ->
                val existing = list.find { it.id == id }
                if (existing != null) {
                    list.map { if (it.id == id) it.copy(title = title, updatedAt = now) else it }
                } else {
                    listOf(Conversation(id = id, title = title, createdAt = now, updatedAt = now)) + list
                }
            }
        }
    }

    override fun updateConversationTimestamp(id: String) {
        val now = currentTimeMillis()
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.updateConversationTimestamp(updatedAt = now, id = id)
            }
        } else {
            _inMemoryConversations.update { list ->
                list.map { if (it.id == id) it.copy(updatedAt = now) else it }
            }
        }
    }

    override fun deleteConversation(id: String) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.deleteMessagesByConversation(id)
                database.bubbleDatabaseQueries.deleteConversation(id)
            }
        } else {
            _inMemoryConversations.update { list -> list.filter { it.id != id } }
            _inMemoryChat.update { list -> list.filter { it.conversationId != id } }
        }
    }

    override fun insertChatMessage(message: ChatMessage) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.insertMessage(
                    id = message.id,
                    conversationId = message.conversationId,
                    sender = message.sender.name,
                    text = message.text,
                    timestamp = message.timestamp,
                    sduiJson = message.sduiJson
                )
            }
        } else {
            _inMemoryChat.update { it + message }
        }
    }

    override fun updateChatMessageText(id: String, text: String) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.updateMessageText(text = text, id = id)
            }
        } else {
            _inMemoryChat.update { list ->
                list.map { if (it.id == id) it.copy(text = text) else it }
            }
        }
    }

    override fun updateChatMessageSdui(id: String, sduiJson: String?) {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.updateMessageSdui(sduiJson = sduiJson, id = id)
            }
        } else {
            _inMemoryChat.update { list ->
                list.map { if (it.id == id) it.copy(sduiJson = sduiJson) else it }
            }
        }
    }

    override fun clearChatMessages() {
        if (database != null) {
            coroutineScope.launch {
                database.bubbleDatabaseQueries.clearAllMessages()
                database.bubbleDatabaseQueries.clearAllConversations()
            }
        } else {
            _inMemoryChat.value = emptyList()
            _inMemoryConversations.value = emptyList()
        }
    }

    override fun refreshSnapshot() {
        _digitalHealthSnapshot.value =
            screenTimeSource.getTodaySnapshot(_offlineProfile.value.interests)
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
