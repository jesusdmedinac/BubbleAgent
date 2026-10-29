package com.bubble.agent.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.bubble.agent.BubbleApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Summary of user's digital health metrics and offline preferences.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class DigitalHealthSnapshotResponse(
    /** Total minutes spent on device screen today. */
    val totalScreenTimeMinutes: Long,
    /** Estimated recoverable minutes that can be replaced by offline activities. */
    val potentialRecoverableMinutes: Long,
    /** Whether actual Android usage stats permission is currently granted. */
    val isPermissionGranted: Boolean,
    /** List of offline interests and hobbies preferred by the user. */
    val offlineInterests: List<String>,
    /** Top applications consuming attention today formatted as appName with minutes. */
    val topAppsSummary: List<String>,
)

/**
 * User offline interests and preferred engagement constraints.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class UserOfflineInterestsResponse(
    /** List of offline hobbies and activities the user enjoys in the real world. */
    val interests: List<String>,
    /** Ideal duration in minutes for an offline activity session. */
    val idealDurationMinutes: Int,
    /** Preferred physical or mental energy level for activities, such as LOW, MEDIUM, or HIGH. */
    val energyLevel: String,
    /** Goals the user wishes to achieve by reducing screen time. */
    val goals: List<String>,
)

/**
 * Parameters for saving a recommended offline activity.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SaveRecommendedActivityParams(
    /** The title of the actionable offline activity. */
    val title: String,
    /** The category of activity, such as MUSIC, FITNESS, NATURE, READING, or SOCIAL. */
    val category: String,
    /** Estimated duration required to complete the activity in minutes. */
    val estimatedMinutes: Int,
    /** The contextual reason why this offline activity helps replace passive screen time. */
    val reason: String,
)

/**
 * Result confirming the registration of a recommended offline activity.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ActivityRecommendationResult(
    /** The unique identifier assigned to the saved activity. */
    val activityId: String,
    /** The saved title of the activity. */
    val title: String,
    /** The category of the activity. */
    val category: String,
    /** Whether the activity was successfully saved in the user's Bubble repository. */
    val isSaved: Boolean,
)

/**
 * MCP Server entry point exposing Bubble's digital wellbeing and offline profile tools to Gemini.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "BubbleAppFunctionService",
    appFunctionXmlFileName = "bubble_app_function_service",
)
abstract class BaseBubbleAppFunctionService : AppFunctionService() {

    private val repository by lazy {
        (applicationContext as BubbleApplication).appContainer.bubbleRepository
    }

    /**
     * Retrieve the current digital health snapshot, including today's screen time and offline interests.
     *
     * @return A [DigitalHealthSnapshotResponse] detailing device screen usage and registered hobbies.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getDigitalHealthSnapshot(): DigitalHealthSnapshotResponse = withContext(Dispatchers.IO) {
        val snapshot = repository.digitalHealthSnapshot.value
        DigitalHealthSnapshotResponse(
            totalScreenTimeMinutes = snapshot.totalScreenTimeMinutes,
            potentialRecoverableMinutes = snapshot.potentialRecoverableMinutes,
            isPermissionGranted = snapshot.isPermissionGranted,
            offlineInterests = snapshot.offlineInterests,
            topAppsSummary = snapshot.topApps.map { "${it.appName} (${it.usageMinutes}m)" }
        )
    }

    /**
     * Retrieve the user's structured profile of offline interests, preferred durations, and goals.
     *
     * @return A [UserOfflineInterestsResponse] containing the user's hobbies and constraints.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getUserOfflineInterests(): UserOfflineInterestsResponse = withContext(Dispatchers.IO) {
        val profile = repository.offlineProfile.value
        UserOfflineInterestsResponse(
            interests = profile.interests,
            idealDurationMinutes = profile.idealDurationMinutes,
            energyLevel = profile.energyLevel,
            goals = profile.goals
        )
    }

    /**
     * Save a personalized offline activity recommendation to replace screen time.
     *
     * @param params The parameters describing the activity title, category, duration, and reason.
     * @return An [ActivityRecommendationResult] confirming that the activity was saved.
     * @throws AppFunctionInvalidArgumentException If the activity title is blank.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun saveRecommendedActivity(
        params: SaveRecommendedActivityParams
    ): ActivityRecommendationResult = withContext(Dispatchers.IO) {
        if (params.title.isBlank()) {
            throw AppFunctionInvalidArgumentException("Activity title must not be blank.")
        }
        val saved = repository.saveRecommendedActivity(
            title = params.title,
            category = params.category,
            estimatedMinutes = params.estimatedMinutes,
            reason = params.reason
        )
        ActivityRecommendationResult(
            activityId = saved.id,
            title = saved.title,
            category = saved.category,
            isSaved = true
        )
    }
}
