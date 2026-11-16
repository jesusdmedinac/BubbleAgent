package com.jesusdmedinac.bubble.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

// On iOS the prompt bar is native SwiftUI, which does its own dictation; this one is never shown.
@Composable
actual fun rememberSpeechInput(onSpokenText: (String) -> Unit): SpeechInput = remember {
    object : SpeechInput {
        override fun start() = Unit
    }
}

@Composable
actual fun rememberShareSheet(): ShareSheet = remember {
    object : ShareSheet {
        override fun share(text: String) {
            val activityController = UIActivityViewController(
                activityItems = listOf(text),
                applicationActivities = null,
            )
            topViewController()?.presentViewController(activityController, animated = true, completion = null)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
actual fun plainTextClipEntry(text: String): ClipEntry = ClipEntry.withPlainText(text)

private fun topViewController(): UIViewController? {
    // UIApplication.keyWindow is nil in scene-based apps; look for the key window of a scene.
    val window = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .flatMap { scene -> scene.windows.filterIsInstance<UIWindow>() }
        .let { windows -> windows.firstOrNull { it.keyWindow } ?: windows.firstOrNull() }
    var controller = window?.rootViewController
    while (controller?.presentedViewController != null) {
        controller = controller.presentedViewController
    }
    return controller
}
