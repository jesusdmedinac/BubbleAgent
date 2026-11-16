package com.jesusdmedinac.bubble.ui.main

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.bubble.data.model.RecommendedActivity
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan15
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan33
import com.jesusdmedinac.bubble.ui.theme.SurfaceContainerLow
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@Composable
fun RecommendedActivitiesSection(
    activities: List<RecommendedActivity>,
    onToggleActivity: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSimulateMcpClick: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Sugerencias de Desconexión",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerLow)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay actividades guardadas. Habla con Bubble o envía una recomendación vía AppFunctions MCP.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            activities.forEach { activity ->
                RecommendedActivityCard(
                    activity = activity,
                    onToggle = { onToggleActivity(activity.id) }
                )
            }
        }
    }
}

@Composable
@Preview
private fun RecommendedActivitiesSectionPreview() {
    RecommendedActivitiesSection(
        activities = listOf(
            RecommendedActivity(
                id = "1",
                title = "Lectura de libro",
                category = "Productividad",
                estimatedMinutes = 15,
                reason = "Basado en tu tiempo libre",
                isCompleted = true,
            )
        ),
        onToggleActivity = {},
        onSimulateMcpClick = {}
    )
}
