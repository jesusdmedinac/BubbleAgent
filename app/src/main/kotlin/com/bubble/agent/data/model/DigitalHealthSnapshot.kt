package com.bubble.agent.data.model

/**
 * Metric details of screen consumption for a specific app package.
 */
data class AppUsageStat(
    val appName: String,
    val packageName: String,
    val usageMinutes: Long
)

/**
 * Snapshot of daily digital consumption versus reclaimable real-world time.
 */
data class DigitalHealthSnapshot(
    val totalScreenTimeMinutes: Long = 0L,
    val potentialRecoverableMinutes: Long = 0L,
    val topApps: List<AppUsageStat> = emptyList(),
    val isPermissionGranted: Boolean = false,
    val offlineInterests: List<String> = emptyList(),
)
