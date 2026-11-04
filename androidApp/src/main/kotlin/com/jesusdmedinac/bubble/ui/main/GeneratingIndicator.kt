package com.jesusdmedinac.bubble.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@Composable
fun GeneratingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(start = 28.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(BubbleCyan)
        )
        Text(
            text = "Generando respuesta local...",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
@Preview
fun GeneratingIndicatorPreview() {
    BubbleTheme {
        Box(modifier = Modifier.background(BgAmoled).padding(16.dp)) {
            GeneratingIndicator()
        }
    }
}