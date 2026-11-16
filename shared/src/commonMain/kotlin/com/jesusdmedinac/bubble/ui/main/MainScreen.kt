package com.jesusdmedinac.bubble.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.jesusdmedinac.bubble.data.model.DigitalHealthSnapshot
import com.jesusdmedinac.bubble.ui.LocalUseNativeNavigation
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    viewModel: MainScreenViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val healthSnapshot by viewModel.digitalHealthSnapshot.collectAsStateWithLifecycle()
    val activities by viewModel.recommendedActivities.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onRefreshUsageStats()
    }

    var isDrawerOpen by remember { mutableStateOf(false) }
    var isAttachSheetOpen by remember { mutableStateOf(false) }
    var isModelSheetOpen by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (LocalUseNativeNavigation.current) {
        // The native shell draws the bars, the menu and the sheets; only the chat stays here.
        Box(modifier = modifier.fillMaxSize().background(BgAmoled)) {
            MainScreenContent(
                uiState = uiState,
                listState = listState,
                modifier = Modifier.fillMaxSize(),
                onInputChanged = viewModel::onInputTextChanged,
                onSendMessage = viewModel::onSendMessage,
                onOpenSettingsClick = { viewModel.onOpenUsageSettings() },
                onToggleActivity = viewModel::onToggleActivityCompleted,
                onSimulateMcpClick = viewModel::simulateGeminiMcpInvocation,
                onCustomAction = viewModel::onCustomAction,
                healthSnapshot = healthSnapshot,
                activities = activities,
            )
        }
        return
    }

    // Back closes the drawer before it leaves the screen.
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = isDrawerOpen,
        onBackCompleted = { isDrawerOpen = false },
    )

    val progress by animateFloatAsState(
        targetValue = if (isDrawerOpen) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "drawer_displacement"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgAmoled)
            .pointerInput(isDrawerOpen) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 25 && !isDrawerOpen) {
                        isDrawerOpen = true
                    } else if (dragAmount < -25 && isDrawerOpen) {
                        isDrawerOpen = false
                    }
                }
            }
    ) {
        // 1. Drawer Layer (Left side navigation pane)
        BubbleMenuDrawer(
            onClose = { isDrawerOpen = false },
            onNewConversation = {
                isDrawerOpen = false
                viewModel.onNewChatClicked()
            },
            onFocusModeClick = {
                isDrawerOpen = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Modo de Enfoque próximamente en la v1.2")
                }
            },
            onAppsClick = {
                isDrawerOpen = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Gestor de aplicaciones de bienestar disponible en la v1.2")
                }
            },
            onChallengesClick = {
                isDrawerOpen = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Retos de desconexión comunitaria disponibles en la v1.2")
                }
            },
            onScreenTimeClick = {
                isDrawerOpen = false
                onProfileClick()
            },
            onConversationClick = { convId ->
                isDrawerOpen = false
                viewModel.onSelectConversation(convId)
            },
            onRecentQueryClick = { query ->
                isDrawerOpen = false
                viewModel.onSelectConversationByTitle(query)
            },
            onProfileClick = {
                isDrawerOpen = false
                onProfileClick()
            },
            onSettingsClick = {
                isDrawerOpen = false
                viewModel.onOpenUsageSettings()
            },
            userName = uiState.userName,
            userPlan = "Pro Account",
            recentConversations = uiState.recentConversations,
            currentConversationId = uiState.currentConversationId,
            recentQueries = uiState.recentQueries,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        )

        // 2. Displaced Main App Screen Layer
        val cornerRadius = (32 * progress).dp
        val borderWidth = (2 * progress).dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = (270.dp * progress).toPx()
                    scaleX = 1f - (0.12f * progress)
                    scaleY = 1f - (0.12f * progress)
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    shape = RoundedCornerShape(cornerRadius)
                    clip = progress > 0.01f
                }
                .then(
                    if (progress > 0.01f) {
                        Modifier
                            .clip(RoundedCornerShape(cornerRadius))
                            .border(borderWidth, BubbleCyan, RoundedCornerShape(cornerRadius))
                    } else Modifier
                )
        ) {
            MainScreenContent(
                uiState = uiState,
                listState = listState,
                modifier = Modifier.fillMaxSize(),
                onMenuClick = { isDrawerOpen = true },
                onRefreshUsageStats = viewModel::onRefreshUsageStats,
                onModelSelect = { isModelSheetOpen = true },
                onNewChat = viewModel::onNewChatClicked,
                onProfileClick = onProfileClick,
                onInputChanged = viewModel::onInputTextChanged,
                onSendMessage = viewModel::onSendMessage,
                onAttachClick = { isAttachSheetOpen = true },
                onLiveAudioClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Audio en vivo disponible próximamente con Android AICore Live")
                    }
                },
                onOpenSettingsClick = { viewModel.onOpenUsageSettings() },
                onToggleActivity = viewModel::onToggleActivityCompleted,
                onSimulateMcpClick = viewModel::simulateGeminiMcpInvocation,
                onCustomAction = viewModel::onCustomAction,
                healthSnapshot = healthSnapshot,
                activities = activities,
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            )

            // When drawer is open, clicking on the displaced app returns to full screen
            if (progress > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            isDrawerOpen = false
                        }
                )
            }
        }

        // 3. Bottom Sheets
        if (isAttachSheetOpen) {
            AttachContextBottomSheet(
                onDismissRequest = { isAttachSheetOpen = false },
                onAttachScreenTime = {
                    isAttachSheetOpen = false
                    viewModel.onInputTextChanged(uiState.inputText + viewModel.screenTimeContext())
                },
                onAttachGoal = {
                    isAttachSheetOpen = false
                    viewModel.onInputTextChanged(OFFLINE_GOAL_PROMPT)
                },
                onAttachScreenshot = {
                    isAttachSheetOpen = false
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Análisis visual multimodal con Gemini Nano disponible en v1.2")
                    }
                }
            )
        }

        if (isModelSheetOpen) {
            BubbleLocalInfoBottomSheet(
                onDismissRequest = { isModelSheetOpen = false }
            )
        }
    }
}

@Preview(name = "MainScreen Preview", showBackground = true)
@Composable
fun MainScreenPreview() {
    BubbleTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BgAmoled)
        ) {
            MainScreenContent(
                uiState = ChatUiState(),
                listState = rememberLazyListState(),
                modifier = Modifier.fillMaxSize(),
                onRefreshUsageStats = {},
                onModelSelect = {},
                onNewChat = {},
                onProfileClick = {},
                onInputChanged = {},
                onSendMessage = {},
                healthSnapshot = DigitalHealthSnapshot(),
                activities = emptyList()
            )
        }
    }
}
