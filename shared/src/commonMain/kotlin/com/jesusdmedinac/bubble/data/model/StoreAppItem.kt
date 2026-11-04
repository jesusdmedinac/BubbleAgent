package com.jesusdmedinac.bubble.data.model

/**
 * An application listed in the Bubble App Store catalog.
 */
data class StoreAppItem(
    val packageName: String,
    val name: String,
    val tagline: String,
    val description: String,
    val category: String,
    val isMindful: Boolean = true,
    val rating: Float = 4.8f,
    val isInstalled: Boolean = false
)
