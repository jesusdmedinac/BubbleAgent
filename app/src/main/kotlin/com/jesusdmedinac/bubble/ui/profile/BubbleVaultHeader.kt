package com.jesusdmedinac.bubble.ui.profile

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Storage
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
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.StatusSuccess
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@Composable
fun BubbleVaultHeader(
    selectedCategory: VaultCategory,
    onCategorySelected: (VaultCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title Row + Status Pill
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
                    imageVector = Icons.Rounded.Storage,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Bubble Vault • Memoria Local",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

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
                    modifier = Modifier.size(10.dp)
                )
                Text(
                    text = "Android MCP Activo",
                    color = StatusSuccess,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        Text(
            text = "Conocimiento y hábitos guardados automáticamente en conversaciones y sensores MCP locales para dar memoria infinita y privada a Gemini Nano.",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontFamily = FontFamily.SansSerif
        )

        // Filter Chips Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = listOf(
                VaultCategory.ALL to "Todas (48)",
                VaultCategory.OFFLINE_GOAL to "Metas Offline (12)",
                VaultCategory.MCP_HABIT to "Hábitos MCP (18)",
                VaultCategory.PREFERENCE to "Preferencias (18)"
            )

            items(chips) { (cat, label) ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) BubbleCyan else Color(0xFF18181C))
                        .then(
                            if (!isSelected) Modifier.border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(8.dp))
                            else Modifier
                        )
                        .clickable { onCategorySelected(cat) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color(0xFF090A0D) else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun BubbleVaultHeaderPreview() {
    BubbleTheme {
        BubbleVaultHeader(
            selectedCategory = VaultCategory.ALL,
            onCategorySelected = {}
        )
    }
}
