package com.jesusdmedinac.bubble.data.model

import com.jesusdmedinac.bubble.core.randomId

enum class VaultCategory(val displayName: String) {
    ALL("Todas"),
    OFFLINE_GOAL("Metas Offline"),
    MCP_HABIT("Hábitos MCP"),
    PREFERENCE("Preferencias")
}

/**
 * Encrypted on-device memory item stored in the Bubble Vault,
 * extracted automatically from user conversations or detected by the Android MCP Server.
 */
data class VaultMemoryItem(
    val id: String = randomId(),
    val tag: String,
    val category: VaultCategory,
    val title: String,
    val source: String,
    val timestampFormatted: String,
    val footerDetail: String,
    val tagColorHex: Long = 0xFF1BB5C6
)
