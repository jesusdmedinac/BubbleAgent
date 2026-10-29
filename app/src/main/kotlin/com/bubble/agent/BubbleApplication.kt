package com.bubble.agent

import android.app.Application
import com.bubble.agent.data.repository.BubbleRepository
import com.bubble.agent.data.repository.BubbleRepositoryImpl
import com.bubble.agent.data.tracker.ScreenTimeTracker

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
