package com.jesusdmedinac.bubble.data.model

import com.jesusdmedinac.bubble.core.currentTimeMillis
import com.jesusdmedinac.bubble.core.randomId

/**
 * An actionable offline activity recommended by Gemini via Android MCP / AppFunctions
 * or through the in-app Bubble agent.
 */
data class RecommendedActivity(
    val id: String = randomId(),
    val title: String = "",
    val category: String = "",
    val estimatedMinutes: Int = 0,
    val reason: String = "",
    val timestamp: Long = currentTimeMillis(),
    val isCompleted: Boolean = false
)
