package com.bubble.agent.mcp.model

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable

/**
 * A snapshot of the user's current digital health and screen time metrics.
 * Shared with Gemini via Android AppFunctions MCP to contextualize recommendations.
 */
@AppFunctionSerializable
@Serializable
data class DigitalHealthSnapshot(
    val totalScreenTimeMinutes: Int,
    val continuousScrollMinutes: Int,
    val topDistractingApp: String,
    val offlineInterests: List<String>,
    val transportationMethod: String,
    val potentialRecoverableMinutes: Int = (continuousScrollMinutes - 15).coerceAtLeast(0)
)
