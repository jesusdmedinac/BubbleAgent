package com.jesusdmedinac.bubble.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

// Placeholders until phase 4 adds the Speech framework and UIActivityViewController.

@Composable
actual fun rememberSpeechInput(onSpokenText: (String) -> Unit): SpeechInput = remember {
    object : SpeechInput {
        override fun start() = Unit
    }
}

@Composable
actual fun rememberShareSheet(): ShareSheet = remember {
    object : ShareSheet {
        override fun share(text: String) = Unit
    }
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry.withPlainText(text)
