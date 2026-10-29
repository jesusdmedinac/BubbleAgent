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
    val totalScreenTimeMinutes: Long,
    val potentialRecoverableMinutes: Long,
    val topApps: List<AppUsageStat>,
    val isPermissionGranted: Boolean,
    val offlineInterests: List<String>
)
