package com.jesusdmedinac.bubble

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** A screen of the app. The title is what the native iOS navigation bar shows for it. */
sealed interface BubbleRoute : NavKey {
    val title: String
}

@Serializable data object Main : BubbleRoute {
    override val title: String get() = "Bubble"
}

@Serializable data object ProfileVault : BubbleRoute {
    override val title: String get() = "Profile & Vault"
}
