package com.jesusdmedinac.bubble.ui.profile

import com.jesusdmedinac.bubble.ui.platformCopy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.ui.LocalNativeChromePadding
import com.jesusdmedinac.bubble.ui.LocalUseNativeNavigation
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.SurfaceContainerLow
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@Composable
fun ProfileVaultContent(
    uiState: ProfileVaultUiState,
    onClose: () -> Unit,
    onCategorySelected: (VaultCategory) -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSecurityDialogOpen by remember { mutableStateOf(false) }

    if (isSecurityDialogOpen) {
        VaultSecurityInfoDialog(onDismissRequest = { isSecurityDialogOpen = false })
    }

    val useNativeNavigation = LocalUseNativeNavigation.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgAmoled)
            .then(
                if (useNativeNavigation) Modifier
                else Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = LocalNativeChromePadding.current,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // 1. Navigation / Top Bar (the native shell draws its own)
                if (!useNativeNavigation) {
                    ProfileVaultTopBar(
                        onClose = onClose,
                        onSecurityShieldClick = { isSecurityDialogOpen = true }
                    )
                }
            }

            item {
                // 2. Card / User Profile & NPU Identity
                UserProfileCard(
                    userName = uiState.userName,
                    userHandle = uiState.userHandle,
                    streakDays = uiState.focusStreakDays,
                    totalMemories = uiState.totalMemoriesCount
                )
            }

            item {
                // 3. Card / Screen Time & Wellbeing
                ScreenTimeCard(
                    hours = uiState.screenTimeHours,
                    minutes = uiState.screenTimeMinutes,
                    trend = uiState.screenTimeTrend,
                    topApps = uiState.topApps,
                    offlineGainText = uiState.offlineGainText,
                    isPermissionGranted = uiState.isUsagePermissionGranted,
                    onOpenSettingsClick = onOpenSettingsClick
                )
            }

            item {
                // 4. Header & Filter for Bubble Vault
                BubbleVaultHeader(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            // 5. Memory Cards List
            if (uiState.displayedMemories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceContainerLow)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tu Vault está vacío. Bubble y Gemini irán guardando hábitos y metas descubiertas durante tus conversaciones.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(uiState.displayedMemories, key = { it.id }) { memory ->
                    VaultMemoryCard(memory = memory)
                }
            }

            item {
                // 6. Box / Android MCP Bridge (AppFunctions exist only on Android)
                if (platformCopy.showsAssistantToolsBridge) AndroidMcpBridgeBox()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(name = "ProfileVaultContent Preview", showBackground = true)
@Composable
fun ProfileVaultContentPreview() {
    BubbleTheme {
        ProfileVaultContent(
            uiState = ProfileVaultUiState(
                userName = "Jesús Medina",
                userHandle = "@jesusmedinac",
                focusStreakDays = 14,
                totalMemoriesCount = 42,
                screenTimeHours = 3,
                screenTimeMinutes = 45,
                screenTimeTrend = "-18% vs ayer",
                offlineGainText = "+1h 15m tiempo recuperado",
                isUsagePermissionGranted = true,
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
