package com.jesusdmedinac.bubble.ui.platform

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry

/*
 * Platform actions used by the shared chat UI. The interfaces and the remember functions are
 * what shared code calls; the bodies below are the Android implementation and become `actual`
 * declarations once the UI moves to commonMain.
 */

/** Starts voice dictation; the recognized text arrives through the callback given to [rememberSpeechInput]. */
interface SpeechInput {
    fun start()
}

/** Opens the system share sheet for a piece of text. */
interface ShareSheet {
    fun share(text: String)
}

@Composable
fun rememberSpeechInput(onSpokenText: (String) -> Unit): SpeechInput {
    val context = LocalContext.current
    val currentOnSpokenText by rememberUpdatedState(onSpokenText)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) currentOnSpokenText(spokenText)
        }
    }
    return remember(launcher, context) {
        object : SpeechInput {
            override fun start() {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            "Habla para dictar a Bubble..."
                        )
                    }
                    launcher.launch(intent)
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "Reconocimiento de voz no disponible en este dispositivo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

@Composable
fun rememberShareSheet(): ShareSheet {
    val context = LocalContext.current
    return remember(context) {
        object : ShareSheet {
            override fun share(text: String) {
                try {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, text)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Compartir respuesta")
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(shareIntent)
                } catch (_: Exception) {
                    // Handled gracefully
                }
            }
        }
    }
}

/** Plain-text clipboard entry for `LocalClipboard`. */
fun plainTextClipEntry(text: String): ClipEntry = ClipData.newPlainText("Bubble", text).toClipEntry()
