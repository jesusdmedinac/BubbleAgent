package com.bubble.agent.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubble.agent.data.model.AppUsageStat
import com.bubble.agent.data.model.VaultCategory
import com.bubble.agent.data.model.VaultMemoryItem
import com.bubble.agent.ui.theme.AccentGold
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.BubbleCyan15
import com.bubble.agent.ui.theme.BubbleCyan25
import com.bubble.agent.ui.theme.BubbleTheme
import com.bubble.agent.ui.theme.StatusSuccess
import com.bubble.agent.ui.theme.TextMuted
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun ProfileVaultScreen(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileVaultViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionAndStats()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(onBack = onClose)

    ProfileVaultContent(
        uiState = uiState,
        onClose = onClose,
        onCategorySelected = viewModel::onCategorySelected,
        onOpenSettingsClick = { viewModel.openUsageSettings(context) },
        modifier = modifier
    )
}

@Composable
fun ProfileVaultContent(
    uiState: ProfileVaultUiState,
    onClose: () -> Unit,
    onCategorySelected: (VaultCategory) -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgAmoled)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // 1. Navigation / Top Bar
                ProfileVaultTopBar(onClose = onClose)
            }

            item {
                // 2. Card / User Profile & NPU Identity
                UserProfileCard(
                    userName = uiState.userName,
                    userHandle = uiState.userHandle,
                    streakDays = uiState.focusStreakDays,
                    totalMemories = uiState.totalMemoriesCount
                )
            }

            item {
                // 3. Card / Screen Time & Wellbeing
                ScreenTimeCard(
                    hours = uiState.screenTimeHours,
                    minutes = uiState.screenTimeMinutes,
                    trend = uiState.screenTimeTrend,
                    topApps = uiState.topApps,
                    offlineGainText = uiState.offlineGainText,
                    isPermissionGranted = uiState.isUsagePermissionGranted,
                    onOpenSettingsClick = onOpenSettingsClick
                )
            }

            item {
                // 4. Header & Filter for Bubble Vault
                BubbleVaultHeader(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            // 5. Memory Cards List
            items(uiState.displayedMemories, key = { it.id }) { memory ->
                VaultMemoryCard(memory = memory)
            }

            item {
                // 6. Box / Android MCP Bridge
                AndroidMcpBridgeBox()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileVaultTopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                contentDescription = "Close Profile",
                tint = TextSecondary,
                modifier = Modifier.size(17.dp)
            )
        }

        Text(
            text = "Profile & Vault",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )

        // Security / Vault Shield Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1B1B1F))
                .border(1.dp, Color(0xFF2E2E33), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = "Security Status",
                tint = BubbleCyan,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun UserProfileCard(
    userName: String,
    userHandle: String,
    streakDays: Int,
    totalMemories: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF121214))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile Info Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1B1F))
                    .border(1.5.dp, BubbleCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = userName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = userHandle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Memory,
                        contentDescription = null,
                        tint = BubbleCyan,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Gemini Nano • Snapdragon NPU",
                        color = BubbleCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Telemetry Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryPill(
                value = "🔥 $streakDays Días",
                label = "Focus Streak",
                labelColor = AccentGold,
                modifier = Modifier.weight(1f)
            )
            TelemetryPill(
                value = "🧠 $totalMemories",
                label = "Memorias Vault",
                labelColor = BubbleCyan,
                modifier = Modifier.weight(1f)
            )
            TelemetryPill(
                value = "🔒 100%",
                label = "Local On-Device",
                labelColor = StatusSuccess,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TelemetryPill(
    value: String,
    label: String,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF18181C))
            .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = label,
            color = labelColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.SansSerif
        )
    }
}

data class DonutSegment(
    val label: String,
    val valueMinutes: Long,
    val color: Color,
    val sweepAngle: Float
)

@Composable
private fun InteractiveDonutChart(
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

            // Background track
            drawArc(
                color = Color(0xFF1E1E24),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx)
            )

            // Segments starting at -90 degrees (12 o'clock / top)
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

@Composable
private fun DonutLegendRow(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier
) {
    val validSegments = segments.filter { it.valueMinutes > 0L }
    val chunkSize = if (validSegments.size > 3) 2 else 3

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        validSegments.chunked(chunkSize).forEach { rowSegments ->
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowSegments.forEach { segment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(segment.color)
                        )
                        Text(
                            text = "${segment.label} ${segment.valueMinutes}m",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScreenTimeCard(
    hours: Long,
    minutes: Long,
    trend: String,
    topApps: List<AppUsageStat>,
    offlineGainText: String,
    isPermissionGranted: Boolean,
    onOpenSettingsClick: () -> Unit
) {
    val totalMinutes = hours * 60 + minutes
    val segments = remember(topApps, totalMinutes, isPermissionGranted) {
        if (totalMinutes <= 0L) {
            listOf(
                DonutSegment("Sin uso", 0L, Color(0xFF2A2A30), 360f)
            )
        } else if (topApps.isEmpty() || !isPermissionGranted) {
            // Exact 360-degree closed distribution matching bubble-system.pen
            listOf(
                DonutSegment("Bubble", 75L, BubbleCyan, 202f),
                DonutSegment("Social", 42L, Color(0xFFFF6B4A), 113f),
                DonutSegment("Otras", 17L, Color(0xFFF5A623), 45f)
            )
        } else {
            val colors = listOf(BubbleCyan, Color(0xFFFF6B4A), Color(0xFFF5A623), Color(0xFF5C8DEF))
            val top3 = topApps.take(3)
            val topSum = top3.sumOf { it.usageMinutes }
            val remaining = (totalMinutes - topSum).coerceAtLeast(0L)

            val list = mutableListOf<DonutSegment>()
            var accumulatedAngle = 0f

            top3.forEachIndexed { idx, app ->
                val fraction = if (totalMinutes > 0) app.usageMinutes.toFloat() / totalMinutes.toFloat() else 0f
                val angle = fraction * 360f
                accumulatedAngle += angle
                list.add(
                    DonutSegment(
                        label = app.appName.take(10),
                        valueMinutes = app.usageMinutes,
                        color = colors.getOrElse(idx) { BubbleCyan },
                        sweepAngle = angle
                    )
                )
            }

            if (remaining > 0L) {
                val remAngle = (360f - accumulatedAngle).coerceAtLeast(0f)
                list.add(
                    DonutSegment(
                        label = "Otras",
                        valueMinutes = remaining,
                        color = colors[3],
                        sweepAngle = remAngle
                    )
                )
            } else if (list.isNotEmpty()) {
                val last = list.removeAt(list.size - 1)
                list.add(last.copy(sweepAngle = 360f - list.sumOf { it.sweepAngle.toDouble() }.toFloat()))
            }
            list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF121214))
            .border(1.dp, Color(0xFF27272A), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Row
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
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Screen Time • Hoy",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            if (isPermissionGranted) {
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
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Sincronizado en Vivo",
                        color = StatusSuccess,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x22F5A623))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "Modo Demostración",
                        color = AccentGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // 2. Centered Donut Chart Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InteractiveDonutChart(
                segments = segments,
                centerTimeText = "${hours}h ${minutes}m",
                centerSubtitleText = "Hoy en pantalla"
            )

            DonutLegendRow(segments = segments)
        }

        // Subtitle / Target
        Text(
            text = if (isPermissionGranted) {
                "Meta diaria: < 3h 00m (En rango saludable)"
            } else {
                "Simulado • Requiere acceso a datos de uso para tiempo real"
            },
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        // 3. Permission Activation Banner (Shown only when permission is NOT granted)
        if (!isPermissionGranted) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1710))
                    .border(1.dp, Color(0x66F5A623), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(0x33F5A623)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Lectura de tiempo en pantalla inactiva",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = "Concede acceso a Bubble en Ajustes",
                            color = AccentGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }

                Text(
                    text = "Permite a Bubble y a Gemini Nano medir tus horas reales y detectar picos de distracción en apps para sugerir desconexiones offline oportunas.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentGold)
                        .clickable { onOpenSettingsClick() }
                        .padding(vertical = 9.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = Color(0xFF090A0D),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "Activar en Ajustes de Android",
                        color = Color(0xFF090A0D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Text(
                        text = "100% privado en tu Snapdragon NPU. Cero telemetría externa.",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }

        // Apps Usage Breakdown Section
        Text(
            text = if (isPermissionGranted) "Apps más usadas hoy" else "Apps de demostración",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif
        )

        if (isPermissionGranted && topApps.isEmpty()) {
            Text(
                text = "Sin uso de apps registrado aún hoy.",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            val appColors = listOf(Color(0xFFFF6B4A), Color(0xFFF5A623), Color(0xFF5C8DEF), BubbleCyan)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                topApps.take(4).forEachIndexed { idx, app ->
                    val barColor = appColors.getOrElse(idx) { BubbleCyan }
                    val progressFraction = (app.usageMinutes / 60f).coerceIn(0.12f, 0.95f)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.appName,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "${app.usageMinutes}m",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Progress Track
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1B1B1F))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }
            }
        }

        // Offline Gain Callout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(BubbleCyan15)
                .border(1.dp, BubbleCyan25, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = BubbleCyan,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = offlineGainText,
                color = BubbleCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
private fun BubbleVaultHeader(
    selectedCategory: VaultCategory,
    onCategorySelected: (VaultCategory) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun VaultMemoryCard(memory: VaultMemoryItem) {
    val tagColor = Color(memory.tagColorHex)

    Column(
        modifier = Modifier
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

@Composable
private fun AndroidMcpBridgeBox() {
    Column(
        modifier = Modifier
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

        // 2 Rows of Tool Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolBadge(tool = "get_screen_time", modifier = Modifier.weight(1f))
            ToolBadge(tool = "list_app_usage", modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToolBadge(tool = "save_vault_memory", modifier = Modifier.weight(1f))
            ToolBadge(tool = "set_focus_rule", modifier = Modifier.weight(1f))
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

@Composable
private fun ToolBadge(tool: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1B1B1F))
            .border(1.dp, Color(0xFF2E2E33), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tool,
            color = BubbleCyan,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Preview
@Composable
fun ProfileVaultScreenPreview() {
    BubbleTheme {
        ProfileVaultContent(
            uiState = ProfileVaultUiState(
                displayedMemories = listOf(
                    VaultMemoryItem(
                        tag = "META OFFLINE",
                        category = VaultCategory.OFFLINE_GOAL,
                        title = "Sustituir doomscrolling por 25 min de guitarra.",
                        source = "Hace 2h • Gemini Nano",
                        timestampFormatted = "Hace 2h",
                        footerDetail = "Origen: Chat local • SQLite Encriptada on-device"
                    )
                )
            ),
            onClose = {},
            onCategorySelected = {},
            onOpenSettingsClick = {}
        )
    }
}
