package com.jesusdmedinac.bubble

import com.jesusdmedinac.bubble.ai.BubbleAiEngine
import com.jesusdmedinac.bubble.data.repository.BubbleRepository
import com.jesusdmedinac.bubble.data.tracker.ScreenTimeSource

/**
 * Dependencies the shared screens need, supplied by each platform's entry point.
 * Android provides [AppContainer].
 */
interface AppGraph {
    val bubbleRepository: BubbleRepository
    val screenTimeSource: ScreenTimeSource
    fun createAiEngine(): BubbleAiEngine
}
