package com.bubble.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
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
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.GlassWhite05
import com.bubble.agent.ui.theme.GlassWhite06
import com.bubble.agent.ui.theme.GlassWhite12
import com.bubble.agent.ui.theme.GlassWhite16
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun BubbleTopAppBar(
    onMenuClick: () -> Unit,
    onModelSelect: () -> Unit,
    onNewChat: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Floating Menu Button (40x40 circle)
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(GlassWhite06)
                .border(1.dp, GlassWhite16, CircleShape)
                .clickable { onMenuClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Selector / AI Model (Pill)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(GlassWhite05)
                .border(1.dp, GlassWhite12, RoundedCornerShape(18.dp))
                .clickable { onModelSelect() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Bubble",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = "Local",
                color = BubbleCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Select Model",
                tint = TextSecondary,
                modifier = Modifier.size(13.dp)
            )
        }

        // Right Action Group (New Chat + Avatar)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .border(1.dp, GlassWhite16, CircleShape)
                    .clickable { onNewChat() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "New Chat",
                    tint = TextPrimary,
                    modifier = Modifier.size(17.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .border(1.dp, GlassWhite16, CircleShape)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun BubbleTopAppBarPreview() {
    BubbleTopAppBar(
        onMenuClick = {},
        onModelSelect = {},
        onNewChat = {},
        onProfileClick = {}
    )
}
