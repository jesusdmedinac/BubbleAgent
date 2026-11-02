package com.jesusdmedinac.bubble.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.GlassWhite06

@Composable
fun LiveAudioWaveOrSendButton(
    inputText: String,
    onSend: () -> Unit,
    onLiveAudioClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = inputText.isNotBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(BubbleCyan)
                    .clickable { onSend() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send Message",
                    tint = BgAmoled,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = inputText.isBlank(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .clickable { onLiveAudioClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = "Live Audio Session",
                    tint = BubbleCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(name = "LiveAudioWaveOrSendButton Preview", showBackground = true)
@Composable
fun LiveAudioWaveOrSendButtonPreview() {
    BubbleTheme {
        LiveAudioWaveOrSendButton(inputText = "", onSend = {})
    }
}
