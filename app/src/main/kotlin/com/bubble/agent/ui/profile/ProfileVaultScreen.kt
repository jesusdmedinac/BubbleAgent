package com.bubble.agent.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubble.agent.data.model.VaultCategory
import com.bubble.agent.data.model.VaultMemoryItem
import com.bubble.agent.ui.theme.BubbleTheme

@Composable
fun ProfileVaultScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileVaultViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionAndStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(onBack = onClose)

    ProfileVaultContent(
        uiState = uiState,
        onClose = onClose,
        onCategorySelected = viewModel::onCategorySelected,
        onOpenSettingsClick = { viewModel.openUsageSettings(context) },
        modifier = modifier
    )
}

@Preview(name = "ProfileVaultScreen Preview", showBackground = true)
@Composable
fun ProfileVaultScreenPreview() {
    BubbleTheme {
        ProfileVaultContent(
            uiState = ProfileVaultUiState(
                displayedMemories = listOf(
                    VaultMemoryItem(
                        tag = "META OFFLINE",
                        category = VaultCategory.OFFLINE_GOAL,
                        title = "Sustituir doomscrolling por 25 min de guitarra.",
                        source = "Hace 2h • Gemini Nano",
                        timestampFormatted = "Hace 2h",
                        footerDetail = "Origen: Chat local • SQLite Encriptada on-device"
                    )
                )
            ),
            onClose = {},
            onCategorySelected = {},
            onOpenSettingsClick = {}
        )
    }
}
