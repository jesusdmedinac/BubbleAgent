package com.bubble.agent.ui.main

import android.content.Context
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubble.agent.ai.GeminiNanoAiEngine

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current.applicationContext,
    viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(GeminiNanoAiEngine(context))
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val healthSnapshot by viewModel.digitalHealthSnapshot.collectAsStateWithLifecycle()
    val activities by viewModel.recommendedActivities.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    MainScreenContent(
        uiState = uiState,
        listState = listState,
        modifier = modifier,
        onRefreshUsageStats = viewModel::onRefreshUsageStats,
        onModelSelect = { },
        onNewChat = viewModel::onNewChatClicked,
        onProfileClick = { },
        onInputChanged = viewModel::onInputTextChanged,
        onSendMessage = viewModel::onSendMessage,
        onOpenSettingsClick = { viewModel.onOpenUsageSettings(context) },
        onToggleActivity = viewModel::onToggleActivityCompleted,
        onSimulateMcpClick = viewModel::simulateGeminiMcpInvocation,
        healthSnapshot = healthSnapshot,
        activities = activities,
    )
}
