package com.jesusdmedinac.bubble

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
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
        val callback = object : AndroidSqliteDriver.Callback(BubbleDatabase.Schema) {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                db.execSQL("CREATE TABLE IF NOT EXISTS ConversationEntity (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                try {
                    db.execSQL("ALTER TABLE ChatMessageEntity ADD COLUMN conversationId TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
                try {
                    db.execSQL("ALTER TABLE ChatMessageEntity ADD COLUMN sduiJson TEXT")
                } catch (_: Exception) {}
            }
        }
        val driver = AndroidSqliteDriver(BubbleDatabase.Schema, application, "bubble.db", callback = callback)
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
