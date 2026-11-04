package com.jesusdmedinac.bubble.data.tracker

import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot

/**
 * iOS does not let apps read other apps' usage freely (decision D3 in KMP_MIGRATION.md),
 * so iOS shows the demo snapshot until a Screen Time integration exists.
 */
class IosScreenTimeSource : ScreenTimeSource {
    override fun hasUsagePermission(): Boolean = false

    override fun getTodaySnapshot(offlineInterests: List<String>): DigitalHealthSnapshot =
        demoScreenTimeSnapshot(offlineInterests)

    override fun openUsageAccessSettings() = Unit
}
