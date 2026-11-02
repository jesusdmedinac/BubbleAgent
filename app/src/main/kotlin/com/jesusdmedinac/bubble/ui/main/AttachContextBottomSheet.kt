package com.jesusdmedinac.bubble.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.jesusdmedinac.bubble.ui.theme.BgAmoled
import com.jesusdmedinac.bubble.ui.theme.BubbleCyan
import com.jesusdmedinac.bubble.ui.theme.BubbleTheme
import com.jesusdmedinac.bubble.ui.theme.GlassWhite06
import com.jesusdmedinac.bubble.ui.theme.GlassWhite12
import com.jesusdmedinac.bubble.ui.theme.SurfaceContainerLow
import com.jesusdmedinac.bubble.ui.theme.TextPrimary
import com.jesusdmedinac.bubble.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachContextBottomSheet(
    onDismissRequest: () -> Unit,
    onAttachScreenTime: () -> Unit,
    onAttachGoal: () -> Unit,
    onAttachScreenshot: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF141418),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(GlassWhite12)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Adjuntar contexto al chat",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )

            Text(
                text = "Proporciona contexto local a Gemini Nano para personalizar tus recomendaciones de bienestar.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontFamily = FontFamily.SansSerif
            )

            Spacer(modifier = Modifier.height(4.dp))

            AttachOptionItem(
                icon = Icons.Rounded.Schedule,
                title = "Insertar datos de tiempo en pantalla de hoy",
                subtitle = "Analiza tus horas y apps más usadas de hoy",
                onClick = {
                    onAttachScreenTime()
                    onDismissRequest()
                }
            )

            AttachOptionItem(
                icon = Icons.Rounded.SelfImprovement,
                title = "Definir una meta de desconexión",
                subtitle = "Planifica tiempo libre sin teléfono ni redes",
                onClick = {
                    onAttachGoal()
                    onDismissRequest()
                }
            )

            AttachOptionItem(
                icon = Icons.Rounded.CameraAlt,
                title = "Captura de pantalla o imagen",
                subtitle = "Análisis visual multimodal (Próximamente v1.2)",
                onClick = {
                    onAttachScreenshot()
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun AttachOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, GlassWhite06, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF202026)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BubbleCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Preview
@Composable
fun AttachContextBottomSheetPreview() {
    BubbleTheme {
        AttachContextBottomSheet(
            onDismissRequest = {},
            onAttachScreenTime = {},
            onAttachGoal = {},
            onAttachScreenshot = {}
        )
    }
}
