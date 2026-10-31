package com.jesusdmedinac.bubble.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Terminal
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
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.StatusSuccess
import com.jesusdmedinac.bubble.ui.theme.TextPrimary

@Composable
fun AndroidMcpBridgeBox(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF18181C))
            .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Terminal,
                contentDescription = null,
                tint = BubbleCyan,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Android MCP Server • Herramientas para Gemini",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Rows of Tool Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolBadge(tool = "search_store_apps", modifier = Modifier.weight(1f))
            ToolBadge(tool = "list_mindful_apps", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolBadge(tool = "get_app_details", modifier = Modifier.weight(1f))
            ToolBadge(tool = "launch_store_app", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolBadge(tool = "get_screen_time", modifier = Modifier.weight(1f))
            ToolBadge(tool = "save_activity", modifier = Modifier.weight(1f))
        }

        Text(
            text = "Conexión local Binder IPC • Sin servidores externos • Cero telemetría",
            color = StatusSuccess,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Preview
@Composable
fun AndroidMcpBridgeBoxPreview() {
    BubbleTheme {
        AndroidMcpBridgeBox()
    }
}
