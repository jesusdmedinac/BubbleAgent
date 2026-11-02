package com.jesusdmedinac.bubble.ui.main

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbDown
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Bubble Assistant Action Bar: containerless micro-action bar (copy, audio, thumbs-up, thumbs-down, share, more).
 * Conforms 100% to the specification:
 * - 32x32dp circular buttons with 15x15dp vector icons and 10dp gap.
 * - Containerless: drawn directly over the dark canvas without heavy container boxes.
 * - Interactive clipboard copy via [LocalClipboard] with dynamic checkmark transition animation (150ms scale/fade, 2s duration).
 * - Full accessibility content descriptions on every touch target.
 */
@Composable
fun BubbleAssistantActionBar(
    modifier: Modifier = Modifier,
    messageText: String = "",
    onCopy: () -> Unit = {},
    onAudio: () -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
) {
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    var likeState by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(2000L)
            isCopied = false
        }
    }

    val handleCopy = {
        if (messageText.isNotEmpty()) {
            coroutineScope.launch {
                val clipEntry = ClipData.newPlainText("Bubble", messageText).toClipEntry()
                clipboard.setClipEntry(clipEntry)
            }
        }
        isCopied = true
        onCopy()
    }

    val handleShare = {
        if (messageText.isNotEmpty()) {
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Compartir respuesta")
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(shareIntent)
            } catch (_: Exception) {
                // Handled gracefully
            }
        }
        onShare()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. Copy Action with animated checkmark transition
        AnimatedContent(
            targetState = isCopied,
            transitionSpec = {
                (scaleIn(initialScale = 0.6f, animationSpec = tween(150)) + fadeIn(animationSpec = tween(150)))
                    .togetherWith(scaleOut(targetScale = 0.6f, animationSpec = tween(150)) + fadeOut(animationSpec = tween(150)))
            },
            label = "CopyCheckmarkTransition",
        ) { copied ->
            if (copied) {
                AssistantActionButton(
                    icon = Icons.Rounded.Check,
                    contentDescription = "Copiado al portapapeles",
                    onClick = handleCopy,
                    tint = BubbleCyan,
                )
            } else {
                AssistantActionButton(
                    icon = Icons.Rounded.ContentCopy,
                    contentDescription = "Copiar respuesta",
                    onClick = handleCopy,
                    tint = TextSecondary,
                )
            }
        }

        // 2. Audio / TTS Action
        AssistantActionButton(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = "Lectura de voz de la respuesta",
            onClick = onAudio,
        )

        // 3. Thumbs Up Action
        AssistantActionButton(
            icon = Icons.Rounded.ThumbUp,
            contentDescription = "Respuesta útil",
            onClick = {
                likeState = if (likeState == true) null else true
                onLike()
            },
            tint = if (likeState == true) BubbleCyan else TextSecondary,
        )

        // 4. Thumbs Down Action
        AssistantActionButton(
            icon = Icons.Rounded.ThumbDown,
            contentDescription = "Respuesta no útil",
            onClick = {
                likeState = if (likeState == false) null else false
                onDislike()
            },
            tint = if (likeState == false) BubbleCyan else TextSecondary,
        )

        // 5. Share Action
        AssistantActionButton(
            icon = Icons.Rounded.Share,
            contentDescription = "Compartir respuesta",
            onClick = handleShare,
        )

        // 6. More Actions
        AssistantActionButton(
            icon = Icons.Rounded.MoreVert,
            contentDescription = "Más opciones",
            onClick = onMore,
        )
    }
}

@Preview
@Composable
private fun BubbleAssistantActionBarPreview() {
    BubbleTheme {
        BubbleAssistantActionBar(
            messageText = "Respuesta de prueba de Bubble",
            onCopy = {},
            onAudio = {},
            onLike = {},
            onDislike = {},
            onShare = {},
            onMore = {},
        )
    }
}
