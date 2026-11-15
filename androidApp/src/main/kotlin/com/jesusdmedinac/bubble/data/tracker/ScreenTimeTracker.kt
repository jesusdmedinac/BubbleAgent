package com.jesusdmedinac.bubble.data.tracker

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.jesusdmedinac.bubble.core.currentTimeMillis
import com.jesusdmedinac.bubble.data.model.AppUsageStat
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import java.util.Calendar

class ScreenTimeTracker(private val context: Context) : ScreenTimeSource {

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

    override fun hasUsagePermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager
            ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Some devices have no usage access screen; the demo data stays visible.
        }
    }

    override fun getTodaySnapshot(offlineInterests: List<String>): DigitalHealthSnapshot {
        val isGranted = hasUsagePermission()
        if (!isGranted) {
            return demoScreenTimeSnapshot(offlineInterests)
        }

        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return demoScreenTimeSnapshot(offlineInterests)

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = calendar.timeInMillis
            val endTime = currentTimeMillis()

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
            demoScreenTimeSnapshot(offlineInterests)
        }
    }
}
