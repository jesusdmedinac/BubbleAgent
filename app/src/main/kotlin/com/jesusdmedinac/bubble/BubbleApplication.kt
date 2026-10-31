package com.jesusdmedinac.bubble

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.data.repository.BubbleRepositoryImpl
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeTracker
import com.jesusdmedinac.bubble.db.BubbleDatabase

class AppContainer(application: Application) {
    val screenTimeTracker: ScreenTimeTracker by lazy {
        ScreenTimeTracker(application)
    }

    val database: BubbleDatabase by lazy {
        val driver = AndroidSqliteDriver(BubbleDatabase.Schema, application, "bubble.db")
        BubbleDatabase(driver)
    }

    val bubbleRepository: BubbleRepository by lazy {
        BubbleRepositoryImpl(
            screenTimeTracker = screenTimeTracker,
            database = database
        )
    }
}

class BubbleApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        instance = this
    }

    companion object {
        lateinit var instance: BubbleApplication
            private set
    }
}
