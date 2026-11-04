package com.jesusdmedinac.bubble.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * True when the platform draws the navigation bar, the prompt bar and the menu natively
 * (SwiftUI with Liquid Glass on iOS). Screens then skip their own Compose versions.
 */
val LocalUseNativeNavigation = staticCompositionLocalOf { false }

/** Space taken by the native bars that float over the Compose content. */
val LocalNativeChromePadding = compositionLocalOf { PaddingValues() }
