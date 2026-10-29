package com.bubble.agent.mcp.model

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable

/**
 * Structured offline interests and lifestyle preferences of the user.
 * Exposed to Gemini via AppFunctions so it suggests real-world activities the user genuinely loves.
 */
@AppFunctionSerializable
@Serializable
data class UserOfflineInterests(
    val interests: List<String>,
    val preferredActivityDurationMinutes: Int = 30,
    val preferredEnergyLevel: String = "Medium",
    val primaryGoal: String = "Reducir doomscrolling y reconectar con pasatiempos físicos"
)
