package com.jesusdmedinac.bubble

import android.app.Application
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.data.repository.BubbleRepositoryImpl
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeTracker

class AppContainer(application: Application) {
    val screenTimeTracker: ScreenTimeTracker by lazy {
        ScreenTimeTracker(application)
    }

    val bubbleRepository: BubbleRepository by lazy {
        BubbleRepositoryImpl(screenTimeTracker)
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
