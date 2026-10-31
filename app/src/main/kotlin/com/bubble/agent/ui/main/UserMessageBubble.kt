package com.bubble.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleTheme
import com.bubble.agent.ui.theme.OnSurface
import com.bubble.agent.ui.theme.SurfaceContainerHigh

@Composable
fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = OnSurface,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
@Preview
fun UserMessageBubblePreview() {
    BubbleTheme {
        Box(modifier = Modifier.background(BgAmoled).padding(16.dp)) {
            UserMessageBubble(
                message = ChatMessage(
                    id = "1",
                    text = "¿Cómo puedo reducir el tiempo de uso de mi teléfono hoy?",
                    sender = MessageSender.USER
                )
            )
        }
    }
}
