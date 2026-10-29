package com.bubble.agent.ui.main

import com.bubble.agent.ai.MockAiEngine
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {

    @Test
    fun uiState_initiallyEmptyMessages() = runTest {
        val viewModel = MainScreenViewModel(MockAiEngine())
        val state = viewModel.uiState.first()
        assertTrue(state.messages.isEmpty())
        assertEquals("Jesús Daniel", state.userName)
    }

    @Test
    fun uiState_onInputTextChanged() = runTest {
        val viewModel = MainScreenViewModel(MockAiEngine())
        viewModel.onInputTextChanged("Hola Bubble")
        assertEquals("Hola Bubble", viewModel.uiState.first().inputText)
    }

    @Test
    fun uiState_sendMessage_clearsInputAndAddsUserAndAssistantMessage() = runTest {
        val viewModel = MainScreenViewModel(MockAiEngine())
        viewModel.onInputTextChanged("Tocar guitarra")
        viewModel.sendMessage()

        val state = viewModel.uiState.first()
        assertEquals("", state.inputText)
        assertEquals(2, state.messages.size)
        assertEquals(MessageSender.USER, state.messages[0].sender)
        assertEquals("Tocar guitarra", state.messages[0].text)
        assertEquals(MessageSender.BUBBLE, state.messages[1].sender)
    }

    @Test
    fun uiState_onNewChatClicked_clearsConversation() = runTest {
        val viewModel = MainScreenViewModel(MockAiEngine())
        viewModel.onInputTextChanged("Mensaje 1")
        viewModel.sendMessage()
        viewModel.onNewChatClicked()

        val state = viewModel.uiState.first()
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.inputText)
    }
}
