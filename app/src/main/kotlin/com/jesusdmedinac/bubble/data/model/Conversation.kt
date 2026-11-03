package com.jesusdmedinac.bubble.data.model

import java.util.UUID

/**
 * Multi-turn conversation entity for grouping chat messages.
 */
data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
