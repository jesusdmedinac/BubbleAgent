package com.jesusdmedinac.bubble.data.tracker

import com.jesusdmedinac.bubble.data.model.AppUsageStat
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot

/**
 * Today's screen time on the current platform. Android reads it from `UsageStatsManager`;
 * a platform that cannot read usage, or a user who has not granted access yet, gets
 * [demoScreenTimeSnapshot].
 */
interface ScreenTimeSource {
    fun hasUsagePermission(): Boolean
    fun getTodaySnapshot(offlineInterests: List<String>): DigitalHealthSnapshot

    /** Opens the system screen where the user grants usage access to Bubble. */
    fun openUsageAccessSettings()
}

/** Realistic sample data shown until real usage can be read. */
fun demoScreenTimeSnapshot(offlineInterests: List<String>): DigitalHealthSnapshot =
    DigitalHealthSnapshot(
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
