package com.jesusdmedinac.bubble.ui.profile

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

data class DonutSegment(
    val label: String,
    val valueMinutes: Long,
    val color: Color,
    val sweepAngle: Float
)

@Composable
fun InteractiveDonutChart(
    segments: List<DonutSegment>,
    centerTimeText: String,
    centerSubtitleText: String = "Hoy en pantalla",
    modifier: Modifier = Modifier,
    size: Dp = 144.dp,
    strokeWidth: Dp = 18.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "donut_sweep_animation"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val strokeWidthPx = with(density) { strokeWidth.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidthPx) / 2f
            val topLeft = Offset(
                (this.size.width - radius * 2f) / 2f,
                (this.size.height - radius * 2f) / 2f
            )
            val arcSize = Size(radius * 2f, radius * 2f)

            // 1. Draw background track ring
            drawArc(
                color = Color(0xFF1E1E24),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx)
            )

            // 2. Draw animated proportional segments starting at 12 o'clock (-90 deg)
            var currentAngle = -90f
            for (segment in segments) {
                val sweep = segment.sweepAngle * animatedProgress
                if (sweep > 0.5f) {
                    drawArc(
                        color = segment.color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                    )
                }
                currentAngle += segment.sweepAngle
            }
        }

        // 3. Central Metric Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = centerTimeText,
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = centerSubtitleText,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview
@Composable
fun InteractiveDonutChartPreview() {
    BubbleTheme {
        InteractiveDonutChart(
            segments = listOf(
                DonutSegment("Bubble", 75L, BubbleCyan, 202f),
                DonutSegment("Social", 42L, Color(0xFFFF6B4A), 113f),
                DonutSegment("Otras", 17L, Color(0xFFF5A623), 45f)
            ),
            centerTimeText = "2h 14m",
            centerSubtitleText = "Hoy en pantalla"
        )
    }
}
