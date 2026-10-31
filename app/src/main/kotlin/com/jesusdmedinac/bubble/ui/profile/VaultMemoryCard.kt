package com.jesusdmedinac.bubble.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
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
import com.jesusdmedinac.bubble.data.model.VaultCategory
import com.jesusdmedinac.bubble.data.model.VaultMemoryItem
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextMuted
import com.jesusdmedinac.bubble.ui.theme.TextPrimary

@Composable
fun VaultMemoryCard(
    memory: VaultMemoryItem,
    modifier: Modifier = Modifier
) {
    val tagColor = Color(memory.tagColorHex)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF121214))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Tag + Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(tagColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = memory.tag,
                    color = tagColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Text(
                text = memory.source,
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Memory Text
        Text(
            text = memory.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 16.sp,
            fontFamily = FontFamily.SansSerif
        )

        // Footer with Lock Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = memory.footerDetail,
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview
@Composable
fun VaultMemoryCardPreview() {
    BubbleTheme {
        VaultMemoryCard(
            memory = VaultMemoryItem(
                tag = "META OFFLINE",
                category = VaultCategory.OFFLINE_GOAL,
                title = "Sustituir doomscrolling nocturno en Instagram por 25 min de práctica de guitarra acústica.",
                source = "Hace 2h • Gemini Nano",
                timestampFormatted = "Hace 2h",
                footerDetail = "Origen: Chat local • SQLite Encriptada on-device",
                tagColorHex = 0xFF1BB5C6
            )
        )
    }
}
