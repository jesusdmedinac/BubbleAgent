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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan15
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary


@Composable
fun BubbleAssistantMessage(
    message: ChatMessage,
    modifier: Modifier = Modifier
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

        Text(
            text = message.text,
            color = TextPrimary,
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 21.sp,
            modifier = Modifier.padding(start = 28.dp)
        )

        // Assistant Action Bar (Copy, Retry, Like)
        Row(
            modifier = Modifier
                .padding(start = 28.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistantActionButton(icon = Icons.Rounded.ContentCopy, contentDescription = "Copy") { }
            AssistantActionButton(icon = Icons.Rounded.Refresh, contentDescription = "Retry") { }
            AssistantActionButton(icon = Icons.Rounded.ThumbUp, contentDescription = "Like") { }
        }
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