package com.jesusdmedinac.bubble.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.bubble.ui.platform.rememberSpeechInput
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.FloatingBarBg
import com.jesusdmedinac.bubble.ui.theme.GlassWhite06
import com.jesusdmedinac.bubble.ui.theme.GlassWhite12
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@Composable
fun FloatingPromptBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    onAttachClick: () -> Unit = {},
    onLiveAudioClick: () -> Unit = {},
) {
    val speechInput = rememberSpeechInput { spokenText ->
        val updated = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
        onInputChanged(updated)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(FloatingBarBg)
            .border(1.dp, GlassWhite12, RoundedCornerShape(26.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Plus Button (Attach/Context)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .clickable { onAttachClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Adjuntar contexto",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Input Field with Prompt Placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Pregunta a Bubble...",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(BubbleCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Voice / Mic Button (Speech-to-Text)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .clickable { speechInput.start() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Dictado por voz",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Live / Audio Wave Button or Send Button
            LiveAudioWaveOrSendButton(
                inputText = inputText,
                onSend = onSend,
                onLiveAudioClick = onLiveAudioClick
            )
        }
    }
}

@Composable
@Preview
fun FloatingPromptBarPreview() {
    BubbleTheme {
        FloatingPromptBar(
            inputText = "",
            onInputChanged = {},
            onSend = {},
            onAttachClick = {},
            onLiveAudioClick = {}
        )
    }
}

