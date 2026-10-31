package com.bubble.agent.data.tracker

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import com.bubble.agent.data.model.AppUsageStat
import com.bubble.agent.data.model.DigitalHealthSnapshot
import java.util.Calendar

class ScreenTimeTracker(private val context: Context) {

    private val ignoredPackages = setOf(
        "android",
        "com.android.systemui",
        "com.samsung.android.honeyboard",
        "com.google.android.inputmethod.latin",
        "com.google.android.gms",
        "com.sec.android.app.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.android.settings",
        "com.samsung.android.settings"
    )

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

            val aggregatedMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
            val statsList: Collection<UsageStats> = if (!aggregatedMap.isNullOrEmpty()) {
                aggregatedMap.values
            } else {
                usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                ) ?: emptyList()
            }

            if (statsList.isEmpty()) {
                return DigitalHealthSnapshot(
                    totalScreenTimeMinutes = 0L,
                    potentialRecoverableMinutes = 0L,
                    topApps = emptyList(),
                    isPermissionGranted = true,
                    offlineInterests = offlineInterests
                )
            }

            val pm = context.packageManager
            val validStats = statsList
                .filter { it.packageName !in ignoredPackages && it.totalTimeInForeground > 30_000L }

            val totalMinutes = validStats.sumOf { it.totalTimeInForeground } / 60_000L

            val appUsageList = validStats
                .sortedByDescending { it.totalTimeInForeground }
                .take(4)
                .map { stat ->
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(stat.packageName, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        stat.packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
                    }
                    val minutes = stat.totalTimeInForeground / 60_000L
                    AppUsageStat(
                        appName = appName,
                        packageName = stat.packageName,
                        usageMinutes = minutes
                    )
                }

            val potentialRecoverable = if (totalMinutes > 0) {
                (totalMinutes * 0.45).toLong().coerceAtLeast(15L)
            } else {
                0L
            }

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
