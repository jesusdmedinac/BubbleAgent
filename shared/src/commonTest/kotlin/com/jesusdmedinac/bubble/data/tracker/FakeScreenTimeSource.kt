package com.jesusdmedinac.bubble.data.tracker

import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot

class FakeScreenTimeSource : ScreenTimeSource {
    var settingsOpenCount = 0
        private set

    override fun hasUsagePermission(): Boolean = false

    override fun getTodaySnapshot(offlineInterests: List<String>): DigitalHealthSnapshot =
        demoScreenTimeSnapshot(offlineInterests)

    override fun openUsageAccessSettings() {
        settingsOpenCount++
    }
}
