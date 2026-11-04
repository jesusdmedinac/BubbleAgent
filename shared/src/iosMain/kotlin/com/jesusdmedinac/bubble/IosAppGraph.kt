package com.jesusdmedinac.bubble

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.jesusdmedinac.bubble.ai.BubbleAiEngine
import com.jesusdmedinac.bubble.ai.MockAiEngine
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.data.repository.BubbleRepositoryImpl
import com.jesusdmedinac.bubble.data.tracker.IosScreenTimeSource
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeSource
import com.jesusdmedinac.bubble.db.BubbleDatabase

/** iOS dependencies. The AI engine is the mock until an on-device model is chosen (decision D2). */
object IosAppGraph : AppGraph {
    override val screenTimeSource: ScreenTimeSource by lazy { IosScreenTimeSource() }

    private val database: BubbleDatabase by lazy {
        BubbleDatabase(NativeSqliteDriver(BubbleDatabase.Schema, "bubble.db"))
    }

    override val bubbleRepository: BubbleRepository by lazy {
        BubbleRepositoryImpl(screenTimeSource = screenTimeSource, database = database)
    }

    override fun createAiEngine(): BubbleAiEngine = MockAiEngine()
}
