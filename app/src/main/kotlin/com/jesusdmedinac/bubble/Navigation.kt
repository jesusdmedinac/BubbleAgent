package com.jesusdmedinac.bubble

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.jesusdmedinac.bubble.ui.main.MainScreen
import com.jesusdmedinac.bubble.ui.main.MainScreenViewModel
import com.jesusdmedinac.bubble.ui.profile.ProfileVaultScreen
import com.jesusdmedinac.bubble.ui.profile.ProfileVaultViewModel

@Composable
fun MainNavigation(appGraph: AppGraph) {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    onProfileClick = { backStack.add(ProfileVault) },
                    viewModel = viewModel {
                        MainScreenViewModel(
                            aiEngine = appGraph.createAiEngine(),
                            repository = appGraph.bubbleRepository,
                            screenTimeSource = appGraph.screenTimeSource
                        )
                    }
                )
            }
            entry<ProfileVault> {
                ProfileVaultScreen(
                    onClose = { backStack.removeLastOrNull() },
                    viewModel = viewModel {
                        ProfileVaultViewModel(
                            repository = appGraph.bubbleRepository,
                            screenTimeSource = appGraph.screenTimeSource
                        )
                    }
                )
            }
        }
    )
}
