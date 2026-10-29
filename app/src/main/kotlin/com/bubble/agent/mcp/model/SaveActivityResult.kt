package com.bubble.agent.mcp.model

import androidx.appfunctions.AppFunctionSerializable
import kotlinx.serialization.Serializable

/**
 * Result returned to Gemini after saving an activity recommendation.
 */
@AppFunctionSerializable
@Serializable
data class SaveActivityResult(
    val success: Boolean,
    val message: String,
    val savedActivityTitle: String
)
