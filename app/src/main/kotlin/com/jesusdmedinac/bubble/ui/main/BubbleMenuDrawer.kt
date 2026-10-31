package com.jesusdmedinac.bubble.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TrackChanges
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
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.Gold20
import com.jesusdmedinac.bubble.ui.theme.TextMuted
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

/**
 * Bubble Menu Drawer based on `bubble-system.pen` (Screen / M3 - 04 Bubble Menu Drawer).
 */
@Composable
fun BubbleMenuDrawer(
    onClose: () -> Unit,
    onNewConversation: () -> Unit,
    onFocusModeClick: () -> Unit = {},
    onAppsClick: () -> Unit = {},
    onChallengesClick: () -> Unit = {},
    onScreenTimeClick: () -> Unit = {},
    onRecentQueryClick: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    userName: String = "Jesús Medina",
    userPlan: String = "Pro Account",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(BgAmoled)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Group
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: App Title + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bubble",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )

                // Close Button (36x36 Circle)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1B1F))
                        .border(1.dp, Color(0xFF2E2E33), CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Menu",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // Button / New Conversation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF18181C))
                    .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(22.dp))
                    .clickable { onNewConversation() }
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "New conversation",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Navigation Links Group
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrawerNavigationLink(
                    icon = Icons.Rounded.TrackChanges,
                    title = "Focus mode",
                    iconTint = TextSecondary,
                    titleColor = TextSecondary,
                    badge = {
                        ComingSoonBadge()
                    },
                    onClick = onFocusModeClick
                )

                DrawerNavigationLink(
                    icon = Icons.Rounded.GridView,
                    title = "Apps",
                    iconTint = TextSecondary,
                    titleColor = TextSecondary,
                    badge = {
                        ComingSoonBadge()
                    },
                    onClick = onAppsClick
                )

                DrawerNavigationLink(
                    icon = Icons.Rounded.EmojiEvents,
                    title = "Challenges",
                    iconTint = TextSecondary,
                    titleColor = TextSecondary,
                    badge = {
                        ComingSoonBadge()
                    },
                    onClick = onChallengesClick
                )

                DrawerNavigationLink(
                    icon = Icons.Rounded.Schedule,
                    title = "Screen time",
                    iconTint = TextSecondary,
                    titleColor = TextSecondary,
                    badge = {
                        ComingSoonBadge()
                    },
                    onClick = onScreenTimeClick
                )
            }

            // Section / Recientes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Recent",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )

                val recentQueries = listOf(
                    "How to reduce Instagram screen time?",
                    "4-7-8 breathing exercise for focus",
                    "Overcoming phone check anxiety",
                    "Digital detox bedtime routine"
                )

                recentQueries.forEach { query ->
                    Text(
                        text = query,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onRecentQueryClick(query) }
                            .padding(vertical = 4.dp, horizontal = 2.dp)
                    )
                }
            }
        }

        // Drawer Bottom / Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onProfileClick() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar Circle (36x36)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1B1B1F))
                        .border(1.dp, Color(0xFF2E2E33), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Profile",
                        tint = TextPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Text Column
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = userName,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = userPlan,
                        color = BubbleCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // Settings Button (36x36 Circle)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(name = "BubbleMenuDrawer Preview", showBackground = true)
@Composable
fun BubbleMenuDrawerPreview() {
    BubbleTheme {
        Box(
            modifier = Modifier
                .background(BgAmoled)
                .padding(16.dp)
        ) {
            BubbleMenuDrawer(
                onClose = {},
                onNewConversation = {}
            )
        }
    }
}
