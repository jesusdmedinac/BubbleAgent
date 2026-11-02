package com.jesusdmedinac.bubble.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

/**
 * Micro-action button for chat interactions in Bubble.
 * Follows containerless design: 32x32dp touch target, circular ripple, 15x15dp centered vector icon.
 */
@Composable
fun AssistantActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextSecondary,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(15.dp),
        )
    }
}

@Composable
@Preview
fun AssistantActionButtonPreview() {
    BubbleTheme {
        AssistantActionButton(
            icon = Icons.Rounded.ThumbUp,
            contentDescription = "Like",
            onClick = {},
            tint = BubbleCyan,
        )
    }
}

