package com.jesusdmedinac.bubble.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.jesusdmedinac.bubble.BubbleApplication
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Complete digital wellbeing profile and vault memory collected by Bubble during user interactions.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class UserProfileResponse(
    /** Total minutes the user spent on device screen today. */
    val totalScreenTimeMinutes: Long,
    /** Estimated recoverable screen time minutes that could be replaced by offline activities. */
    val potentialRecoverableMinutes: Long,
    /** Top apps consuming attention today formatted as app name with minutes. */
    val topAppsSummary: List<String>,
    /** Whether Android screen time usage permission is currently granted. */
    val isPermissionGranted: Boolean,
    /** Offline hobbies and real-world interests registered in the profile. */
    val offlineInterests: List<String>,
    /** Personal offline goals the user wants to achieve. */
    val goals: List<String>,
    /** Ideal duration in minutes for an offline activity session. */
    val idealDurationMinutes: Int,
    /** Preferred physical or mental energy level for activities (e.g. LOW, MEDIUM, HIGH). */
    val energyLevel: String,
    /** Key insights, habits, and memories collected in Bubble's vault during past conversations. */
    val vaultMemories: List<String>,
)

/**
 * Parameters for updating the user profile and saving a new vault insight from Gemini.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SaveUserProfileParams(
    /** A useful insight, habit observation, or contextual fact to store in Bubble's vault. */
    val insight: String,
    /** The category classification: OFFLINE_GOAL, MCP_HABIT, or PREFERENCE. */
    val category: String,
    /** An optional offline hobby or interest to add to the profile, or empty string if none. */
    val newInterest: String,
    /** An optional personal goal to add to the profile, or empty string if none. */
    val newGoal: String,
)

/**
 * Result confirming the update of the user profile and vault memory.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SaveUserProfileResult(
    /** Whether the profile and vault were successfully updated. */
    val isSuccess: Boolean,
    /** The insight saved into the vault. */
    val savedInsight: String,
    /** The category assigned to the new vault entry. */
    val category: String,
    /** Updated list of interests in the user profile. */
    val currentInterests: List<String>,
    /** Updated list of goals in the user profile. */
    val currentGoals: List<String>,
    /** Informational status message confirming the update. */
    val statusMessage: String,
)

/**
 * MCP Server entry point exposing Bubble's user profile and vault to Gemini.
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
     * Read the user profile including daily screen time metrics and memories collected in the vault.
     *
     * @return A [UserProfileResponse] detailing screen time, offline interests, goals, and vault memories.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun readUserProfile(): UserProfileResponse = withContext(Dispatchers.IO) {
        val snapshot = repository.digitalHealthSnapshot.value
        val profile = repository.offlineProfile.value
        val memories = repository.vaultMemories.value

        UserProfileResponse(
            totalScreenTimeMinutes = snapshot.totalScreenTimeMinutes,
            potentialRecoverableMinutes = snapshot.potentialRecoverableMinutes,
            topAppsSummary = snapshot.topApps.map { "${it.appName} (${it.usageMinutes}m)" },
            isPermissionGranted = snapshot.isPermissionGranted,
            offlineInterests = profile.interests,
            goals = profile.goals,
            idealDurationMinutes = profile.idealDurationMinutes,
            energyLevel = profile.energyLevel,
            vaultMemories = memories.map { "[${it.tag}] ${it.title}" }
        )
    }

    /**
     * Update the user profile with information and insights that Gemini considers useful to build a better vault profile for Bubble.
     *
     * @param params Parameters containing the insight to store in the vault, category, and any newly discovered interest or goal.
     * @return A [SaveUserProfileResult] confirming the update.
     * @throws AppFunctionInvalidArgumentException If the insight is blank.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun saveUserProfile(
        params: SaveUserProfileParams
    ): SaveUserProfileResult = withContext(Dispatchers.IO) {
        if (params.insight.isBlank()) {
            throw AppFunctionInvalidArgumentException("Insight parameter must not be blank.")
        }

        val categoryEnum = when (params.category.trim().uppercase()) {
            "OFFLINE_GOAL" -> VaultCategory.OFFLINE_GOAL
            "MCP_HABIT" -> VaultCategory.MCP_HABIT
            else -> VaultCategory.PREFERENCE
        }

        val tag = when (categoryEnum) {
            VaultCategory.OFFLINE_GOAL -> "META OFFLINE"
            VaultCategory.MCP_HABIT -> "HÁBITO DETECTADO"
            VaultCategory.PREFERENCE, VaultCategory.ALL -> "PREFERENCIA"
        }

        val tagColor = when (categoryEnum) {
            VaultCategory.OFFLINE_GOAL -> 0xFF1BB5C6
            VaultCategory.MCP_HABIT -> 0xFFF5A623
            VaultCategory.PREFERENCE, VaultCategory.ALL -> 0xFF5C8DEF
        }

        val memoryItem = VaultMemoryItem(
            tag = tag,
            category = categoryEnum,
            title = params.insight.trim(),
            source = "Ahora • Gemini",
            timestampFormatted = "Ahora",
            footerDetail = "Origen: AppFunction saveUserProfile • Sincronizado en Vault",
            tagColorHex = tagColor
        )
        repository.addVaultMemory(memoryItem)

        if (params.newInterest.isNotBlank()) {
            repository.addInterest(params.newInterest.trim())
        }

        if (params.newGoal.isNotBlank()) {
            val currentProfile = repository.offlineProfile.value
            if (!currentProfile.goals.contains(params.newGoal.trim())) {
                repository.updateProfile(
                    currentProfile.copy(goals = currentProfile.goals + params.newGoal.trim())
                )
            }
        }

        val updatedProfile = repository.offlineProfile.value

        SaveUserProfileResult(
            isSuccess = true,
            savedInsight = memoryItem.title,
            category = categoryEnum.name,
            currentInterests = updatedProfile.interests,
            currentGoals = updatedProfile.goals,
            statusMessage = "Perfil y Vault de Bubble actualizados exitosamente."
        )
    }
}
