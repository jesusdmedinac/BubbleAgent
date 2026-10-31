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
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.BubbleCyan10
import com.bubble.agent.ui.theme.BubbleCyan15
import com.bubble.agent.ui.theme.BubbleCyan20
import com.bubble.agent.ui.theme.GlassWhite05
import com.bubble.agent.ui.theme.GlassWhite12
import com.bubble.agent.ui.theme.SurfaceContainerHigh
import com.bubble.agent.ui.theme.SurfaceContainerLow
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun DigitalHealthCard(
    snapshot: DigitalHealthSnapshot,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, GlassWhite12, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row: Title + Live/Mock chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BubbleCyan15),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = BubbleCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Tiempo de Pantalla Hoy",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                if (!snapshot.isPermissionGranted) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BubbleCyan10)
                            .border(1.dp, BubbleCyan20, RoundedCornerShape(12.dp))
                            .clickable { onOpenSettingsClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Acceso",
                            color = BubbleCyan,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassWhite05)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "En Vivo (UsageStats)",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            // Stats Row: Total vs Recoverable
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${snapshot.totalScreenTimeMinutes / 60}h ${snapshot.totalScreenTimeMinutes % 60}m",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Consumo total",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${snapshot.potentialRecoverableMinutes / 60}h ${snapshot.potentialRecoverableMinutes % 60}m",
                        color = BubbleCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Tiempo potencial recuperable",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // Top apps badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                snapshot.topApps.take(4).forEach { app ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${app.appName} ${app.usageMinutes}m",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun DigitalHealthCardPreview() {
    DigitalHealthCard(
        snapshot = DigitalHealthSnapshot(
            totalScreenTimeMinutes = 185,
            potentialRecoverableMinutes = 60,
            isPermissionGranted = true
        ),
        onOpenSettingsClick = {}
    )
}
