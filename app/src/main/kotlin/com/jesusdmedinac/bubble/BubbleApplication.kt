package com.jesusdmedinac.bubble

import android.app.Application
import android.database.sqlite.SQLiteDatabase
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
        markHandPatchedDatabaseAsMigrated(application)
        val driver = AndroidSqliteDriver(BubbleDatabase.Schema, application, DATABASE_NAME)
        BubbleDatabase(driver)
    }

    val bubbleRepository: BubbleRepository by lazy {
        BubbleRepositoryImpl(
            screenTimeTracker = screenTimeTracker,
            database = database
        )
    }
}

private const val DATABASE_NAME = "bubble.db"

/**
 * Builds made before migrations/1.sqm patched the version 1 schema by hand every time the
 * database opened, so those databases already have the version 2 tables and columns while
 * still reporting version 1. Marking them as version 2 stops the migration from adding the
 * same columns again.
 */
private fun markHandPatchedDatabaseAsMigrated(application: Application) {
    val file = application.getDatabasePath(DATABASE_NAME)
    if (!file.exists()) return
    SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
        if (db.version != 1) return
        val messageColumns = db.rawQuery("PRAGMA table_info(ChatMessageEntity)", null).use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildSet { while (cursor.moveToNext()) add(cursor.getString(nameIndex)) }
        }
        if ("conversationId" in messageColumns && "sduiJson" in messageColumns) {
            db.version = 2
        }
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
