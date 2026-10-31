package com.bubble.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleTheme
import com.bubble.agent.ui.theme.OnSurfaceVariant
import com.bubble.agent.ui.theme.SurfaceContainerLow

@Composable
fun AssistantActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(SurfaceContainerLow)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
@Preview
fun AssistantActionButtonPreview() {
    BubbleTheme {
        Box(modifier = Modifier.background(BgAmoled).padding(16.dp)) {
            AssistantActionButton(icon = Icons.Rounded.ThumbUp, contentDescription = "Like") {
            }
        }
    }
}
