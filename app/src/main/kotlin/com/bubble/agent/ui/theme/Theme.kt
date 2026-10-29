package com.bubble.agent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BubbleColorScheme = darkColorScheme(
    primary = BubbleCyan,
    onPrimary = BgAmoled,
    primaryContainer = BubbleCyan15,
    onPrimaryContainer = BubbleCyan,
    secondary = TextSecondary,
    onSecondary = TextPrimary,
    background = BgAmoled,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = OutlineVariant
)

@Composable
fun BubbleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BubbleColorScheme,
        typography = Typography,
        content = content
    )
}
