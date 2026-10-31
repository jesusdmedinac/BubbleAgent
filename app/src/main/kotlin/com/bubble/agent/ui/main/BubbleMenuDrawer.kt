package com.bubble.agent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bubble.agent.ui.theme.AccentGold
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.BubbleCyan15
import com.bubble.agent.ui.theme.BubbleTheme
import com.bubble.agent.ui.theme.Gold20
import com.bubble.agent.ui.theme.TextMuted
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

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
                    iconTint = TextPrimary,
                    titleColor = TextPrimary,
                    onClick = onFocusModeClick
                )

                DrawerNavigationLink(
                    icon = Icons.Rounded.GridView,
                    title = "Apps",
                    iconTint = TextPrimary,
                    titleColor = TextPrimary,
                    badge = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Gold20)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO",
                                color = AccentGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        }
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

@Composable
private fun DrawerNavigationLink(
    icon: ImageVector,
    title: String,
    iconTint: Color,
    titleColor: Color,
    badge: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                color = titleColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }

        badge?.invoke()
    }
}

@Composable
private fun ComingSoonBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BubbleCyan15)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Coming soon",
            color = BubbleCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Preview
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
