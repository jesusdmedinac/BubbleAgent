package com.jesusdmedinac.bubble.ui.profile

import com.jesusdmedinac.bubble.ui.platformCopy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.bubble.ui.theme.AccentGold
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.StatusSuccess
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@Composable
fun UserProfileCard(
    userName: String,
    userHandle: String,
    streakDays: Int,
    totalMemories: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF121214))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1B1F))
                    .border(1.5.dp, BubbleCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = userName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = userHandle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = BubbleCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = platformCopy.deviceEngine,
                        color = BubbleCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Telemetry Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryPill(
                value = "🔥 $streakDays Días",
                label = "Focus Streak",
                labelColor = AccentGold,
                modifier = Modifier.weight(1f)
            )
            TelemetryPill(
                value = "🧠 $totalMemories",
                label = "Memorias Vault",
                labelColor = BubbleCyan,
                modifier = Modifier.weight(1f)
            )
            TelemetryPill(
                value = "🔒 100%",
                label = "Local On-Device",
                labelColor = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview
@Composable
fun UserProfileCardPreview() {
    BubbleTheme {
        UserProfileCard(
            userName = "Jesús Daniel Medina",
            userHandle = "@jesusdmedinac • Pro Account",
            streakDays = 7,
            totalMemories = 48
        )
    }
}
