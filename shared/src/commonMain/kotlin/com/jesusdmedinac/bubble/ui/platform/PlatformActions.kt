package com.jesusdmedinac.bubble.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ClipEntry

/** Platform actions used by the shared chat UI. Each platform provides its own implementation. */

/** Starts voice dictation; the recognized text arrives through the callback given to [rememberSpeechInput]. */
interface SpeechInput {
    fun start()
}

/** Opens the system share sheet for a piece of text. */
interface ShareSheet {
    fun share(text: String)
}

@Composable
expect fun rememberSpeechInput(onSpokenText: (String) -> Unit): SpeechInput

@Composable
expect fun rememberShareSheet(): ShareSheet

/** Plain-text clipboard entry for `LocalClipboard`. */
expect fun plainTextClipEntry(text: String): ClipEntry
