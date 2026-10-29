package com.bubble.agent.data.tracker

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import com.bubble.agent.data.model.AppUsageStat
import com.bubble.agent.data.model.DigitalHealthSnapshot
import java.util.Calendar

class ScreenTimeTracker(private val context: Context) {

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun createSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun getTodaySnapshot(offlineInterests: List<String>): DigitalHealthSnapshot {
        val isGranted = hasUsageStatsPermission()
        if (!isGranted) {
            return getFallbackDemoSnapshot(offlineInterests)
        }

        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return getFallbackDemoSnapshot(offlineInterests)

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStats.isNullOrEmpty()) {
                return getFallbackDemoSnapshot(offlineInterests)
            }

            val pm = context.packageManager
            val appUsageList = usageStats
                .filter { it.totalTimeInForeground > 60_000L } // More than 1 min
                .sortedByDescending { it.totalTimeInForeground }
                .take(4)
                .map { stat ->
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        stat.packageName.substringAfterLast('.')
                    }
                    val minutes = stat.totalTimeInForeground / (1000 * 60)
                    AppUsageStat(
                        appName = appName,
                        packageName = stat.packageName,
                        usageMinutes = minutes
                    )
                }

            val totalMinutes = appUsageList.sumOf { it.usageMinutes }
            // Recoverable time estimated as ~45% of passive screen consumption
            val potentialRecoverable = (totalMinutes * 0.45).toLong().coerceAtLeast(30L)

            DigitalHealthSnapshot(
                totalScreenTimeMinutes = totalMinutes,
                potentialRecoverableMinutes = potentialRecoverable,
                topApps = appUsageList,
                isPermissionGranted = true,
                offlineInterests = offlineInterests
            )
        } catch (e: Exception) {
            getFallbackDemoSnapshot(offlineInterests)
        }
    }

    private fun getFallbackDemoSnapshot(offlineInterests: List<String>): DigitalHealthSnapshot {
        return DigitalHealthSnapshot(
            totalScreenTimeMinutes = 255L, // 4h 15m
            potentialRecoverableMinutes = 120L, // 2h 00m
            topApps = listOf(
                AppUsageStat("Instagram", "com.instagram.android", 105L),
                AppUsageStat("TikTok", "com.zhiliaoapp.musically", 70L),
                AppUsageStat("X / Twitter", "com.twitter.android", 50L),
                AppUsageStat("YouTube", "com.google.android.youtube", 30L)
            ),
            isPermissionGranted = false,
            offlineInterests = offlineInterests
        )
    }
}
