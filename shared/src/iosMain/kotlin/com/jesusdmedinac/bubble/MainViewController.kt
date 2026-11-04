package com.jesusdmedinac.bubble

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.jesusdmedinac.bubble.ui.LocalNativeChromePadding
import com.jesusdmedinac.bubble.ui.LocalUseNativeNavigation
import com.jesusdmedinac.bubble.ui.main.MainScreen
import com.jesusdmedinac.bubble.ui.profile.ProfileVaultScreen
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import platform.UIKit.UIViewController

/*
 * iOS entry points. SwiftUI owns the NavigationStack (Liquid Glass bars); each destination is
 * one of these view controllers with Compose drawing only the screen content.
 */

/** Root chat screen. Navigation requests from Compose go to Swift through [onNavigate]. */
@Suppress("unused")
fun MainViewController(
    bridge: BubbleIosBridge,
    insets: ChromeInsets,
    onNavigate: (BubbleRoute) -> Unit,
): UIViewController = ComposeUIViewController {
    NativeScreen(insets) {
        MainScreen(
            onProfileClick = { onNavigate(ProfileVault) },
            viewModel = bridge.chat,
        )
    }
}

/** A screen pushed on the native NavigationStack. */
@Suppress("unused")
fun ScreenViewController(
    bridge: BubbleIosBridge,
    route: BubbleRoute,
    insets: ChromeInsets,
    onBack: () -> Unit,
): UIViewController = ComposeUIViewController {
    NativeScreen(insets) {
        when (route) {
            Main -> MainScreen(viewModel = bridge.chat)
            ProfileVault -> ProfileVaultScreen(onClose = onBack, viewModel = bridge.vault)
        }
    }
}

@Composable
private fun NativeScreen(insets: ChromeInsets, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalUseNativeNavigation provides true,
        LocalNativeChromePadding provides PaddingValues(top = insets.top.dp, bottom = insets.bottom.dp),
    ) {
        BubbleTheme(content = content)
    }
}
