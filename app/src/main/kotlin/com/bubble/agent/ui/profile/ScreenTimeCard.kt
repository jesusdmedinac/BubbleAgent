package com.bubble.agent.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.agent.data.model.AppUsageStat
import com.bubble.agent.ui.theme.AccentGold
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.BubbleCyan15
import com.bubble.agent.ui.theme.BubbleCyan25
import com.bubble.agent.ui.theme.BubbleTheme
import com.bubble.agent.ui.theme.StatusSuccess
import com.bubble.agent.ui.theme.TextMuted
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun ScreenTimeCard(
    hours: Long,
    minutes: Long,
    trend: String,
    topApps: List<AppUsageStat>,
    offlineGainText: String,
    isPermissionGranted: Boolean,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMinutes = hours * 60 + minutes
    val segments = remember(topApps, totalMinutes, isPermissionGranted) {
        if (totalMinutes <= 0L) {
            listOf(
                DonutSegment("Sin uso", 0L, Color(0xFF2A2A30), 360f)
            )
        } else if (topApps.isEmpty() || !isPermissionGranted) {
            // Exact 360-degree closed distribution matching bubble-system.pen
            listOf(
                DonutSegment("Bubble", 75L, BubbleCyan, 202f),
                DonutSegment("Social", 42L, Color(0xFFFF6B4A), 113f),
                DonutSegment("Otras", 17L, Color(0xFFF5A623), 45f)
            )
        } else {
            val colors = listOf(BubbleCyan, Color(0xFFFF6B4A), Color(0xFFF5A623), Color(0xFF5C8DEF))
            val top3 = topApps.take(3)
            val topSum = top3.sumOf { it.usageMinutes }
            val remaining = (totalMinutes - topSum).coerceAtLeast(0L)

            val list = mutableListOf<DonutSegment>()
            var accumulatedAngle = 0f

            top3.forEachIndexed { idx, app ->
                val fraction = if (totalMinutes > 0) app.usageMinutes.toFloat() / totalMinutes.toFloat() else 0f
                val angle = fraction * 360f
                accumulatedAngle += angle
                list.add(
                    DonutSegment(
                        label = app.appName.take(10),
                        valueMinutes = app.usageMinutes,
                        color = colors.getOrElse(idx) { BubbleCyan },
                        sweepAngle = angle
                    )
                )
            }

            if (remaining > 0L) {
                val remAngle = (360f - accumulatedAngle).coerceAtLeast(0f)
                list.add(
                    DonutSegment(
                        label = "Otras",
                        valueMinutes = remaining,
                        color = colors[3],
                        sweepAngle = remAngle
                    )
                )
            } else if (list.isNotEmpty()) {
                val last = list.removeAt(list.size - 1)
                list.add(last.copy(sweepAngle = 360f - list.sumOf { it.sweepAngle.toDouble() }.toFloat()))
            }
            list
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF121214))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Screen Time • Hoy",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            if (isPermissionGranted) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2210B981))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Sincronizado en Vivo",
                        color = StatusSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22F5A623))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Modo Demostración",
                        color = AccentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // 2. Centered Donut Chart Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InteractiveDonutChart(
                segments = segments,
                centerTimeText = "${hours}h ${minutes}m",
                centerSubtitleText = "Hoy en pantalla"
            )

            DonutLegendRow(segments = segments)
        }

        // Subtitle / Target
        Text(
            text = if (isPermissionGranted) {
                "Meta diaria: < 3h 00m (En rango saludable)"
            } else {
                "Simulado • Requiere acceso a datos de uso para tiempo real"
            },
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        // 3. Permission Activation Banner (Shown only when permission is NOT granted)
        if (!isPermissionGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1710))
                    .border(1.dp, Color(0x66F5A623), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0x33F5A623)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Lectura de tiempo en pantalla inactiva",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Concede acceso a Bubble en Ajustes",
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Text(
                    text = "Permite a Bubble y a Gemini Nano medir tus horas reales y detectar picos de distracción en apps para sugerir desconexiones offline oportunas.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentGold)
                        .clickable { onOpenSettingsClick() }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = Color(0xFF090A0D),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Activar en Ajustes de Android",
                        color = Color(0xFF090A0D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "100% privado en tu Snapdragon NPU. Cero telemetría externa.",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // Apps Usage Breakdown Section
        Text(
            text = if (isPermissionGranted) "Apps más usadas hoy" else "Apps de demostración",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )

        if (isPermissionGranted && topApps.isEmpty()) {
            Text(
                text = "Sin uso de apps registrado aún hoy.",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            val appColors = listOf(Color(0xFFFF6B4A), Color(0xFFF5A623), Color(0xFF5C8DEF), BubbleCyan)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topApps.take(4).forEachIndexed { idx, app ->
                    val barColor = appColors.getOrElse(idx) { BubbleCyan }
                    val progressFraction = (app.usageMinutes / 60f).coerceIn(0.12f, 0.95f)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "${app.usageMinutes}m",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Progress Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1B1B1F))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        }

        // Offline Gain Callout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(BubbleCyan15)
                .border(1.dp, BubbleCyan25, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = BubbleCyan,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = offlineGainText,
                color = BubbleCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview
@Composable
fun ScreenTimeCardPreview() {
    BubbleTheme {
        ScreenTimeCard(
            hours = 2,
            minutes = 14,
            trend = "-38% vs ayer",
            topApps = listOf(
                AppUsageStat("Instagram", "com.instagram.android", 42L),
                AppUsageStat("YouTube", "com.google.android.youtube", 28L),
                AppUsageStat("X (Twitter)", "com.twitter.android", 18L)
            ),
            offlineGainText = "+1h 45m de vida offline ganada (Guitarra & Lectura)",
            isPermissionGranted = true,
            onOpenSettingsClick = {}
        )
    }
}
