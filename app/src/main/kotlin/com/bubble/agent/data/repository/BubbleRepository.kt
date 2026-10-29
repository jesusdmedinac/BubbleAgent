package com.bubble.agent.data.repository

import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.OfflineProfile
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.data.tracker.ScreenTimeTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface BubbleRepository {
    val offlineProfile: StateFlow<OfflineProfile>
    val recommendedActivities: StateFlow<List<RecommendedActivity>>
    val digitalHealthSnapshot: StateFlow<DigitalHealthSnapshot>

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
