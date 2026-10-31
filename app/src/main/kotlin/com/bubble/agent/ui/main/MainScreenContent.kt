package com.bubble.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleTheme
import com.bubble.agent.ui.theme.GlassWhite06
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun MainScreenContent(
    uiState: ChatUiState,
    listState: LazyListState,
    modifier: Modifier,
    onRefreshUsageStats: () -> Unit,
    onModelSelect: () -> Unit,
    onNewChat: () -> Unit,
    onProfileClick: () -> Unit,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    healthSnapshot: DigitalHealthSnapshot,
    activities: List<RecommendedActivity>
) {
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgAmoled,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BubbleTopAppBar(
                onMenuClick = onRefreshUsageStats,
                onModelSelect = onModelSelect,
                onNewChat = onNewChat,
                onProfileClick = onProfileClick,
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            BottomInteractiveStack(
                inputText = uiState.inputText,
                onInputChanged = onInputChanged,
                onSend = onSendMessage
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Top Section: Digital Health Snapshot Card
            item {
                DigitalHealthCard(
                    snapshot = healthSnapshot,
                    onOpenSettingsClick = { /* Open usage settings */ }
                )
            }

            // 2. Middle Section: Recommended Activities from Gemini (Android MCP / AppFunctions)
            item {
                RecommendedActivitiesSection(
                    activities = activities,
                    onToggleActivity = { /* Toggle activity */ },
                    onSimulateMcpClick = { /* Simulate MCP click */ }
                )
            }

            // 3. Conversation Section Header / Empty Greeting
            if (uiState.messages.isEmpty()) {
                item {
                    EmptyChatCanvas(
                        userName = uiState.userName,
                        onStarterClick = { starter ->
                            onInputChanged(starter)
                            onSendMessage()
                        }
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(GlassWhite06)
                        )
                        Text(
                            text = "CHAT CON BUBBLE AGENT",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(GlassWhite06)
                        )
                    }
                }

                items(uiState.messages, key = { it.id }) { msg ->
                    when (msg.sender) {
                        MessageSender.USER -> UserMessageBubble(message = msg)
                        MessageSender.BUBBLE -> BubbleAssistantMessage(message = msg)
                    }
                }
                if (uiState.isGenerating) {
                    item {
                        GeneratingIndicator()
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
@Preview
fun MainScreenContentPreview() {
    BubbleTheme {
        MainScreenContent(
            uiState = ChatUiState(),
            listState = rememberLazyListState(),
            modifier = Modifier,
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
