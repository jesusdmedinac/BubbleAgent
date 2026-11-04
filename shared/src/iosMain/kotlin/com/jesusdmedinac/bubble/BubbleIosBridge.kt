package com.jesusdmedinac.bubble

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jesusdmedinac.bubble.ui.main.ChatUiState
import com.jesusdmedinac.bubble.ui.main.MainScreenViewModel
import com.jesusdmedinac.bubble.ui.main.OFFLINE_GOAL_PROMPT
import com.jesusdmedinac.bubble.ui.profile.ProfileVaultViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * What the SwiftUI shell uses to drive the shared chat. Swift and the Compose screens share the
 * same ViewModels through this object, and state reaches Swift on the main thread through [watch].
 */
class BubbleIosBridge(private val appGraph: AppGraph = IosAppGraph) {
    private val scope = MainScope()

    val chat: MainScreenViewModel = MainScreenViewModel(
        aiEngine = appGraph.createAiEngine(),
        repository = appGraph.bubbleRepository,
        screenTimeSource = appGraph.screenTimeSource,
    )

    val vault: ProfileVaultViewModel by lazy {
        ProfileVaultViewModel(
            repository = appGraph.bubbleRepository,
            screenTimeSource = appGraph.screenTimeSource,
        )
    }

    val currentState: ChatUiState
        get() = chat.uiState.value

    fun watch(onChange: (ChatUiState) -> Unit) {
        scope.launch {
            chat.uiState.collect { onChange(it) }
        }
    }

    /** Sends [text] as the next message. The native prompt bar owns the draft until it is sent. */
    fun send(text: String) {
        if (text.isBlank()) return
        chat.onInputTextChanged(text)
        chat.onSendMessage()
    }

    fun newConversation() = chat.onNewChatClicked()

    fun selectConversation(id: String) = chat.onSelectConversation(id)

    fun screenTimeContext(): String = chat.screenTimeContext()

    val offlineGoalPrompt: String get() = OFFLINE_GOAL_PROMPT

    fun close() = scope.cancel()
}

/** Space, in points, taken by the native bars of one screen. Swift updates it as they change size. */
class ChromeInsets {
    internal var top by mutableStateOf(0.0)
    internal var bottom by mutableStateOf(0.0)

    fun update(top: Double, bottom: Double) {
        this.top = top
        this.bottom = bottom
    }
}
