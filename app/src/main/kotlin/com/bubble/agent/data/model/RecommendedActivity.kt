package com.bubble.agent.data.model

import java.util.UUID

/**
 * An actionable offline activity recommended by Gemini via Android MCP / AppFunctions
 * or through the in-app Bubble agent.
 */
data class RecommendedActivity(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val estimatedMinutes: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
