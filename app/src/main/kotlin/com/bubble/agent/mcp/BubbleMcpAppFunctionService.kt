package com.bubble.agent.mcp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bubble.agent.data.BubbleDataRepository
import com.bubble.agent.mcp.model.DigitalHealthSnapshot
import com.bubble.agent.mcp.model.SaveActivityResult
import com.bubble.agent.mcp.model.UserOfflineInterests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Bubble MCP (Model Context Protocol) Server for Gemini and the Android Intelligence System.
 * Exposes digital health telemetry and accepts actionable real-world recommendations
 * through Android AppFunctions.
 */
class BubbleMcpAppFunctionService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = BubbleDataRepository

    companion object {
        private const val TAG = "BubbleMCP"
        const val ACTION_EXECUTE = "androidx.appfunctions.action.EXECUTE_APP_FUNCTION"
        const val EXTRA_FUNCTION_ID = "androidx.appfunctions.extra.FUNCTION_ID"
        const val EXTRA_FUNCTION_PARAMETERS = "androidx.appfunctions.extra.FUNCTION_PARAMETERS"
        private const val CHANNEL_ID = "bubble_mcp_channel"
        private const val NOTIFICATION_ID = 2001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceNotification()

        val action = intent?.action
        val functionId = intent?.getStringExtra(EXTRA_FUNCTION_ID) ?: ""
        val paramsJson = intent?.getStringExtra(EXTRA_FUNCTION_PARAMETERS)

        Log.i(TAG, "⚡ MCP Request received [action=$action, functionId=$functionId]")

        serviceScope.launch {
            try {
                when {
                    functionId.endsWith("getDigitalHealthSnapshot") -> {
                        val snapshot = getDigitalHealthSnapshot()
                        Log.i(TAG, "✅ [MCP:getDigitalHealthSnapshot] ScreenTime=${snapshot.totalScreenTimeMinutes}m | Scroll=${snapshot.continuousScrollMinutes}m | TopApp=${snapshot.topDistractingApp} | Interests=${snapshot.offlineInterests.size}")
                    }

                    functionId.endsWith("getUserOfflineInterests") -> {
                        val interests = getUserOfflineInterests()
                        Log.i(TAG, "✅ [MCP:getUserOfflineInterests] Returned ${interests.interests.size} interests: ${interests.interests}")
                    }

                    functionId.endsWith("saveRecommendedActivity") -> {
                        val (title, category, minutes, reason) = parseActivityParams(paramsJson)
                        val result = saveRecommendedActivity(title, category, minutes, reason)
                        Log.i(TAG, "✅ [MCP:saveRecommendedActivity] Saved: '${result.savedActivityTitle}' in '$category' (${minutes}m) - Reason: $reason")
                    }

                    else -> {
                        Log.w(TAG, "⚠️ [MCP:UnknownFunction] Received unhandled functionId: $functionId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ [MCP:Error] Failed to execute $functionId: ${e.message}", e)
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Retrieves the user's current digital health snapshot.
     * Call this function to understand screen time accumulation and habits
     * BEFORE generating a real-world activity recommendation.
     */
    fun getDigitalHealthSnapshot(): DigitalHealthSnapshot {
        return repository.getSnapshot()
    }

    /**
     * Retrieves the user's structured list of offline passions, hobbies, and goals.
     * Call this function to personalize activity recommendations.
     */
    fun getUserOfflineInterests(): UserOfflineInterests {
        return repository.getUserOfflineInterests()
    }

    /**
     * Saves a successfully accepted real-world activity recommendation into the user's Bubble profile.
     * ONLY call this function when recommending a high-value offline action to replace screen time.
     *
     * @param title A short, actionable title for the activity (e.g., 'Tocar guitarra acústica').
     * @param category The category (e.g., 'Música', 'Ejercicio', 'Bienestar', 'Lectura').
     * @param estimatedMinutes Estimated time to complete the activity in minutes.
     * @param reason The motivating reason why Gemini recommends this activity.
     */
    fun saveRecommendedActivity(
        title: String,
        category: String,
        estimatedMinutes: Int = 20,
        reason: String = "Recomendado por Gemini para sustituir tiempo en pantalla"
    ): SaveActivityResult {
        return repository.saveRecommendedActivity(title, category, estimatedMinutes, reason)
    }

    private fun parseActivityParams(jsonStr: String?): Tuple4<String, String, Int, String> {
        if (jsonStr.isNullOrBlank()) {
            return Tuple4("Salir a caminar 15 minutos", "Bienestar", 15, "Pausa activa recomendada")
        }
        return try {
            val json = JSONObject(jsonStr)
            val title = json.optString("title", "Actividad del mundo real")
            val category = json.optString("category", "Bienestar")
            val minutes = json.optInt("estimatedMinutes", 20)
            val reason = json.optString("reason", "Recomendado por Gemini vía MCP")
            Tuple4(title, category, minutes, reason)
        } catch (e: Exception) {
            Tuple4("Actividad offline", "Bienestar", 20, "Recomendado por Gemini")
        }
    }

    private fun startForegroundServiceNotification() {
        try {
            val notification = Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Bubble Agent • MCP")
                .setContentText("Procesando comando de inteligencia artificial...")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Notification warning: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bubble MCP Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
