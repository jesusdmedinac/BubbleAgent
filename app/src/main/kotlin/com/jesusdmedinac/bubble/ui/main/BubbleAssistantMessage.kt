package com.jesusdmedinac.bubble.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.bubble.R
import com.jesusdmedinac.bubble.data.model.ChatMessage
import com.jesusdmedinac.bubble.data.model.MessageSender
import com.jesusdmedinac.bubble.ui.main.sdui.BubbleSduiTemplates
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan15
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary
import com.jesusdmedinac.jsontocompose.model.ComposeAction
import com.jesusdmedinac.jsontocompose.model.ComposeDocument
import com.jesusdmedinac.jsontocompose.runtime.LocalCustomActionHandlers
import com.jesusdmedinac.jsontocompose.runtime.ToCompose
import kotlinx.serialization.json.Json

@Composable
fun BubbleAssistantMessage(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    onCopy: () -> Unit = {},
    onAudio: () -> Unit = {},
    onLike: () -> Unit = {},
    onDislike: () -> Unit = {},
    onShare: () -> Unit = {},
    onMore: () -> Unit = {},
    onCustomAction: ((ComposeAction.Custom) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_bubble_phone),
                contentDescription = "Bubble Avatar",
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Bubble",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BubbleCyan15)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "LOCAL",
                    color = BubbleCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (message.text.isNotEmpty()) {
            Text(
                text = message.text,
                color = TextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 21.sp,
                modifier = Modifier.padding(start = 28.dp)
            )
        }

        // Render dynamic SDUI card via json-to-compose if present
        val dynamicDoc = message.dynamicDocument ?: message.sduiJson?.let { jsonStr ->
            try {
                Json.decodeFromString<ComposeDocument>(jsonStr)
            } catch (_: Exception) {
                null
            }
        }

        if (dynamicDoc != null) {
            val customHandlers = buildMap<String, (ComposeAction.Custom) -> Unit> {
                if (onCustomAction != null) {
                    put(BubbleSduiTemplates.ACTION_CONFIRM_VAULT, onCustomAction)
                    put(BubbleSduiTemplates.ACTION_DISMISS_VAULT, onCustomAction)
                }
            }

            CompositionLocalProvider(
                LocalCustomActionHandlers provides customHandlers
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, top = 2.dp, bottom = 4.dp)
                ) {
                    dynamicDoc.ToCompose()
                }
            }
        }

        // Assistant Action Bar containerless
        BubbleAssistantActionBar(
            messageText = message.text,
            onCopy = onCopy,
            onAudio = onAudio,
            onLike = onLike,
            onDislike = onDislike,
            onShare = onShare,
            onMore = onMore,
            modifier = Modifier
                .padding(start = 28.dp)
                .padding(top = 4.dp)
        )
    }
}

@Composable
@Preview
fun BubbleAssistantMessagePreview() {
    BubbleTheme {
        Box(modifier = Modifier.background(BgAmoled).padding(16.dp)) {
            BubbleAssistantMessage(
                message = ChatMessage(
                    id = "1",
                    text = "Hola, ¿en qué puedo ayudarte hoy con tu bienestar digital?",
                    sender = MessageSender.BUBBLE
                )
            )
        }
    }
}
