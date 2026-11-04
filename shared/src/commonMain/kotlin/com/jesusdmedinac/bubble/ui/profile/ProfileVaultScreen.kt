package com.jesusdmedinac.bubble.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme

@Composable
fun ProfileVaultScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileVaultViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermissionAndStats()
    }

    ProfileVaultContent(
        uiState = uiState,
        onClose = onClose,
        onCategorySelected = viewModel::onCategorySelected,
        onOpenSettingsClick = { viewModel.openUsageSettings() },
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
