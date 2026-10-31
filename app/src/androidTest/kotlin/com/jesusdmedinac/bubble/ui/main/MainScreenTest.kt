package com.jesusdmedinac.bubble.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.jesusdmedinac.bubble.ui.main.MainScreen]. */
class MainScreenTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        composeTestRule.setContent { MainScreen() }
    }

    @Test
    fun emptyCanvas_greetingAndPrivacyChipExist() {
        composeTestRule.onNodeWithText("Gemini Nano On-Device • 100% Privado").assertExists()
    }
}
