package com.jesusdmedinac.bubble.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.jesusdmedinac.bubble.BubbleApplication
import com.jesusdmedinac.bubble.ai.MockAiEngine
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.jesusdmedinac.bubble.ui.main.MainScreen]. */
class MainScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        val appContainer = (composeTestRule.activity.application as BubbleApplication).appContainer
        val viewModel = MainScreenViewModel(
            aiEngine = MockAiEngine(),
            repository = appContainer.bubbleRepository,
            screenTimeSource = appContainer.screenTimeSource
        )
        composeTestRule.setContent {
            MainScreen(viewModel = viewModel)
        }
    }

    @Test
    fun emptyCanvas_greetingAndPrivacyChipExist() {
        composeTestRule.onNodeWithText("Gemini Nano On-Device • 100% Privado").assertExists()
    }
}
