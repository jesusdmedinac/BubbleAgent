package com.jesusdmedinac.bubble

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jesusdmedinac.bubble.ui.main.MainScreen
import com.jesusdmedinac.bubble.ui.profile.ProfileVaultScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    onProfileClick = { backStack.add(ProfileVault) }
                )
            }
            entry<ProfileVault> {
                ProfileVaultScreen(
                    onClose = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
