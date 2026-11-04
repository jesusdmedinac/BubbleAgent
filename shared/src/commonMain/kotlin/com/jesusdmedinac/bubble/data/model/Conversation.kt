package com.jesusdmedinac.bubble.data.model

import com.jesusdmedinac.bubble.core.currentTimeMillis
import com.jesusdmedinac.bubble.core.randomId

/**
 * Multi-turn conversation entity for grouping chat messages.
 */
data class Conversation(
    val id: String = randomId(),
    val title: String,
    val createdAt: Long = currentTimeMillis(),
    val updatedAt: Long = currentTimeMillis()
)
