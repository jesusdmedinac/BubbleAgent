package com.bubble.agent.mcp.model

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * An offline activity recommended by Gemini via Android MCP to replace screen time.
 */
@AppFunctionSerializable
@Serializable
data class RecommendedActivity(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val estimatedMinutes: Int = 20,
    val reason: String = "Recomendado por Gemini para reemplazar scroll continuo",
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
)
