package com.bubble.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.BubbleCyan10
import com.bubble.agent.ui.theme.GlassWhite06
import com.bubble.agent.ui.theme.GlassWhite12
import com.bubble.agent.ui.theme.GlassWhite16
import com.bubble.agent.ui.theme.SurfaceContainerLow
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun RecommendedActivityCard(
    activity: RecommendedActivity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(
                1.dp,
                if (activity.isCompleted) GlassWhite06 else GlassWhite12,
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (activity.isCompleted) BubbleCyan else GlassWhite06)
                    .border(
                        1.dp,
                        if (activity.isCompleted) BubbleCyan else GlassWhite16,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (activity.isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = BgAmoled,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BubbleCyan10)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activity.category,
                            color = BubbleCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${activity.estimatedMinutes} min",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Text(
                    text = activity.title,
                    color = if (activity.isCompleted) TextSecondary else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else null
                )

                if (activity.reason.isNotEmpty()) {
                    Text(
                        text = activity.reason,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun RecommendedActivityCardPreview() {
    RecommendedActivityCard(
        activity = RecommendedActivity(
            id = "1",
            title = "Lectura de libro",
            category = "Productividad",
            estimatedMinutes = 15,
            reason = "Basado en tu tiempo libre",
            isCompleted = false
        ),
        onToggle = {}
    )
}
