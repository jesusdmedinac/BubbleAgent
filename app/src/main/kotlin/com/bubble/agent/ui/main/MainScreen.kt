package com.bubble.agent.ui.main

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubble.agent.ai.GeminiNanoAiEngine
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleCyan

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    context: Context = LocalContext.current.applicationContext,
    viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(GeminiNanoAiEngine(context))
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val healthSnapshot by viewModel.digitalHealthSnapshot.collectAsStateWithLifecycle()
    val activities by viewModel.recommendedActivities.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    var isDrawerOpen by remember { mutableStateOf(false) }

    // Intercept hardware back button when drawer is open
    BackHandler(enabled = isDrawerOpen) {
        isDrawerOpen = false
    }

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
            onFocusModeClick = { isDrawerOpen = false },
            onAppsClick = { isDrawerOpen = false },
            onChallengesClick = { isDrawerOpen = false },
            onScreenTimeClick = {
                isDrawerOpen = false
                onProfileClick()
            },
            onRecentQueryClick = { query ->
                isDrawerOpen = false
                viewModel.onInputTextChanged(query)
                viewModel.onSendMessage()
            },
            onProfileClick = {
                isDrawerOpen = false
                onProfileClick()
            },
            onSettingsClick = {
                isDrawerOpen = false
                viewModel.onOpenUsageSettings(context)
            },
            userName = uiState.userName,
            userPlan = "Pro Account",
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
                onModelSelect = { },
                onNewChat = viewModel::onNewChatClicked,
                onProfileClick = onProfileClick,
                onInputChanged = viewModel::onInputTextChanged,
                onSendMessage = viewModel::onSendMessage,
                onOpenSettingsClick = { viewModel.onOpenUsageSettings(context) },
                onToggleActivity = viewModel::onToggleActivityCompleted,
                onSimulateMcpClick = viewModel::simulateGeminiMcpInvocation,
                healthSnapshot = healthSnapshot,
                activities = activities,
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
    }
}
