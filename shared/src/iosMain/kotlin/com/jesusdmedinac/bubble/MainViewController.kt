package com.jesusdmedinac.bubble

import androidx.compose.ui.window.ComposeUIViewController
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import platform.UIKit.UIViewController

/**
 * 100% Compose Multiplatform entry point for iOS.
 * Runs the full shared application with Jetpack Navigation 3, Compose top/bottom bars,
 * and the shared displaced canvas drawer.
 */
@Suppress("unused")
fun MainViewController(): UIViewController = ComposeUIViewController {
    BubbleTheme {
        MainNavigation(appGraph = IosAppGraph)
    }
}
