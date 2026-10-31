package com.jesusdmedinac.bubble.appfunctions

import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionInvalidArgumentException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.jesusdmedinac.bubble.BubbleApplication
import com.jesusdmedinac.bubble.data.model.StoreAppItem
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
 * Information regarding an application listed in the Bubble App Store.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class StoreAppItemResponse(
    /** The unique package name identifier of the application. */
    val packageName: String,
    /** The public display title of the application. */
    val name: String,
    /** Short descriptive tagline summarizing the app value. */
    val tagline: String,
    /** The category classification, such as MUSIC, HEALTH, UTILITY, EDUCATION, or CREATIVITY. */
    val category: String,
    /** Whether this application promotes low-distraction, mindful digital usage. */
    val isMindful: Boolean,
    /** User rating score on a 5.0 scale. */
    val rating: Double,
    /** Whether this application is installed on the local device. */
    val isInstalled: Boolean,
)

/**
 * Parameters for searching applications in the Bubble App Store catalog.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SearchStoreAppsParams(
    /** Keywords to search against app titles, descriptions, and categories. */
    val query: String,
    /** Optional category filter to narrow down results. */
    val category: String? = null,
)

/**
 * Search response containing matched applications from the Bubble App Store.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class StoreAppsSearchResponse(
    /** Total number of apps matching the search criteria. */
    val totalCount: Int,
    /** List of matched applications. */
    val apps: List<StoreAppItemResponse>,
)

/**
 * Curated collection of mindful and offline-supportive applications.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class MindfulAppsResponse(
    /** Header title describing the curated collection. */
    val collectionTitle: String,
    /** Curated applications in the collection. */
    val apps: List<StoreAppItemResponse>,
)

/**
 * Parameters to fetch detailed information about a specific store application.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class GetStoreAppDetailsParams(
    /** The Android package name identifier to look up. */
    val packageName: String,
)

/**
 * Full detailed specification of a Bubble App Store application.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class StoreAppDetailsResponse(
    /** Unique package name identifier. */
    val packageName: String,
    /** Name of the application. */
    val name: String,
    /** Short promotional tagline. */
    val tagline: String,
    /** Comprehensive description of the app functionality and benefits. */
    val description: String,
    /** Primary category. */
    val category: String,
    /** Whether the application is built for mindful, intentional use. */
    val isMindful: Boolean,
    /** Rating on a 5.0 scale. */
    val rating: Double,
    /** Whether the app is installed locally. */
    val isInstalled: Boolean,
)

/**
 * Parameters to launch an installed application on device.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class LaunchStoreAppParams(
    /** Android package name of the application to launch. */
    val packageName: String,
)

/**
 * Result of an application launch attempt.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class LaunchStoreAppResult(
    /** The package name requested. */
    val packageName: String,
    /** Whether the application was successfully launched into the foreground. */
    val isSuccess: Boolean,
    /** Informational status message explaining outcome. */
    val statusMessage: String,
)

/**
 * MCP Server entry point exposing Bubble's digital wellbeing and App Store tools to Gemini.
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

    private fun checkInstalled(packageName: String): Boolean {
        return try {
            applicationContext.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun StoreAppItem.toResponse(): StoreAppItemResponse {
        return StoreAppItemResponse(
            packageName = packageName,
            name = name,
            tagline = tagline,
            category = category,
            isMindful = isMindful,
            rating = rating.toDouble(),
            isInstalled = checkInstalled(packageName) || isInstalled
        )
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

    /**
     * Search the Bubble App Store catalog for applications by query keywords and category.
     *
     * @param params Query terms and optional category filter.
     * @return A [StoreAppsSearchResponse] containing the list of matching applications.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun searchStoreApps(
        params: SearchStoreAppsParams
    ): StoreAppsSearchResponse = withContext(Dispatchers.IO) {
        val results = repository.searchStoreApps(params.query, params.category)
        StoreAppsSearchResponse(
            totalCount = results.size,
            apps = results.map { it.toResponse() }
        )
    }

    /**
     * List curated mindful and low-distraction applications available in the Bubble App Store.
     *
     * @return A [MindfulAppsResponse] with the curated mindful catalog.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listMindfulApps(): MindfulAppsResponse = withContext(Dispatchers.IO) {
        val mindfulApps = repository.listMindfulApps()
        MindfulAppsResponse(
            collectionTitle = "Colección Curada • Apps Mindful & Offline",
            apps = mindfulApps.map { it.toResponse() }
        )
    }

    /**
     * Retrieve complete description and metadata of a specific application in the Bubble App Store.
     *
     * @param params The package name identifier to look up.
     * @return A [StoreAppDetailsResponse] with full application details.
     * @throws AppFunctionInvalidArgumentException If the package name is blank or not found.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getStoreAppDetails(
        params: GetStoreAppDetailsParams
    ): StoreAppDetailsResponse = withContext(Dispatchers.IO) {
        if (params.packageName.isBlank()) {
            throw AppFunctionInvalidArgumentException("Package name must not be blank.")
        }
        val app = repository.getStoreAppDetails(params.packageName)
            ?: throw AppFunctionInvalidArgumentException("Application '${params.packageName}' was not found in Bubble App Store.")
        StoreAppDetailsResponse(
            packageName = app.packageName,
            name = app.name,
            tagline = app.tagline,
            description = app.description,
            category = app.category,
            isMindful = app.isMindful,
            rating = app.rating.toDouble(),
            isInstalled = checkInstalled(app.packageName) || app.isInstalled
        )
    }

    /**
     * Launch or open an application on the user's Android device.
     *
     * @param params The package name of the application to open.
     * @return A [LaunchStoreAppResult] indicating whether the launch succeeded.
     * @throws AppFunctionInvalidArgumentException If the package name is blank.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun launchStoreApp(
        params: LaunchStoreAppParams
    ): LaunchStoreAppResult = withContext(Dispatchers.Main) {
        if (params.packageName.isBlank()) {
            throw AppFunctionInvalidArgumentException("Package name must not be blank.")
        }
        val pm = applicationContext.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(params.packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(launchIntent)
            LaunchStoreAppResult(
                packageName = params.packageName,
                isSuccess = true,
                statusMessage = "Aplicación '${params.packageName}' iniciada con éxito."
            )
        } else {
            LaunchStoreAppResult(
                packageName = params.packageName,
                isSuccess = false,
                statusMessage = "La aplicación '${params.packageName}' no está instalada o no tiene actividad de inicio."
            )
        }
    }
}
