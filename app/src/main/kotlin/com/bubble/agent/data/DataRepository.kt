package com.bubble.agent.data

import com.bubble.agent.mcp.model.DigitalHealthSnapshot
import com.bubble.agent.mcp.model.RecommendedActivity
import com.bubble.agent.mcp.model.SaveActivityResult
import com.bubble.agent.mcp.model.UserOfflineInterests
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface DataRepository {
    val recommendedActivities: StateFlow<List<RecommendedActivity>>
    val offlineInterests: StateFlow<List<String>>
    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot>

    fun getSnapshot(): DigitalHealthSnapshot
    fun getUserOfflineInterests(): UserOfflineInterests
    fun saveRecommendedActivity(
        title: String,
        category: String,
        estimatedMinutes: Int = 20,
        reason: String = "Pausa recomendada por Gemini"
    ): SaveActivityResult
    fun addOfflineInterest(interest: String)
    fun toggleActivityCompleted(activityId: String)
}

/**
 * Singleton repository holding reactive state for both the UI and the MCP AppFunctions service.
 */
object BubbleDataRepository : DataRepository {

    private val defaultInterests = listOf(
        "Práctica de guitarra acústica",
        "Salir a caminar al aire libre",
        "Lectura de libro físico",
        "Entrenamiento de fuerza y estiramientos",
        "Cocinar una receta casera"
    )

    private val initialActivities = listOf(
        RecommendedActivity(
            title = "Tocar guitarra acústica",
            category = "Música / Creatividad",
            estimatedMinutes = 25,
            reason = "Recomendado por Gemini tras 35m de scroll continuo en TikTok"
        ),
        RecommendedActivity(
            title = "Caminata de 15 min al parque",
            category = "Bienestar / Exterior",
            estimatedMinutes = 15,
            reason = "Recomendado para despejar la vista y recuperar energía"
        )
    )

    private val _recommendedActivities = MutableStateFlow<List<RecommendedActivity>>(initialActivities)
    override val recommendedActivities: StateFlow<List<RecommendedActivity>> = _recommendedActivities.asStateFlow()

    private val _offlineInterests = MutableStateFlow<List<String>>(defaultInterests)
    override val offlineInterests: StateFlow<List<String>> = _offlineInterests.asStateFlow()

    private val _digitalHealthSnapshot = MutableStateFlow(
        DigitalHealthSnapshot(
            totalScreenTimeMinutes = 185,
            continuousScrollMinutes = 35,
            topDistractingApp = "TikTok",
            offlineInterests = defaultInterests,
            transportationMethod = "Caminando"
        )
    )
    override val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot> = _digitalHealthSnapshot.asStateFlow()

    override fun getSnapshot(): DigitalHealthSnapshot {
        return _digitalHealthSnapshot.value.copy(
            offlineInterests = _offlineInterests.value
        )
    }

    override fun getUserOfflineInterests(): UserOfflineInterests {
        return UserOfflineInterests(
            interests = _offlineInterests.value
        )
    }

    override fun saveRecommendedActivity(
        title: String,
        category: String,
        estimatedMinutes: Int,
        reason: String
    ): SaveActivityResult {
        require(title.isNotBlank()) { "El título no puede estar vacío" }

        val newActivity = RecommendedActivity(
            title = title,
            category = category,
            estimatedMinutes = estimatedMinutes,
            reason = reason
        )

        _recommendedActivities.update { current ->
            listOf(newActivity) + current
        }

        return SaveActivityResult(
            success = true,
            message = "¡Actividad '$title' guardada exitosamente en Bubble!",
            savedActivityTitle = title
        )
    }

    override fun addOfflineInterest(interest: String) {
        if (interest.isBlank()) return
        _offlineInterests.update { current ->
            if (current.contains(interest)) current else current + interest
        }
    }

    override fun toggleActivityCompleted(activityId: String) {
        _recommendedActivities.update { current ->
            current.map { activity ->
                if (activity.id == activityId) {
                    activity.copy(isCompleted = !activity.isCompleted)
                } else {
                    activity
                }
            }
        }
    }
}
