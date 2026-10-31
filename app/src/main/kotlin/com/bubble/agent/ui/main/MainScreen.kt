package com.bubble.agent.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubble.agent.R
import com.bubble.agent.ai.GeminiNanoAiEngine
import com.bubble.agent.data.model.DigitalHealthSnapshot
import com.bubble.agent.data.model.RecommendedActivity
import com.bubble.agent.ui.theme.BgAmoled
import com.bubble.agent.ui.theme.BubbleCyan
import com.bubble.agent.ui.theme.BubbleCyan10
import com.bubble.agent.ui.theme.BubbleCyan15
import com.bubble.agent.ui.theme.BubbleCyan20
import com.bubble.agent.ui.theme.BubbleCyan33
import com.bubble.agent.ui.theme.FloatingBarBg
import com.bubble.agent.ui.theme.GlassWhite05
import com.bubble.agent.ui.theme.GlassWhite06
import com.bubble.agent.ui.theme.GlassWhite12
import com.bubble.agent.ui.theme.GlassWhite16
import com.bubble.agent.ui.theme.OnSurface
import com.bubble.agent.ui.theme.OnSurfaceVariant
import com.bubble.agent.ui.theme.SurfaceContainerHigh
import com.bubble.agent.ui.theme.SurfaceContainerLow
import com.bubble.agent.ui.theme.TextPrimary
import com.bubble.agent.ui.theme.TextSecondary

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    context: android.content.Context = LocalContext.current.applicationContext,
    viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(GeminiNanoAiEngine(context))
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val healthSnapshot by viewModel.digitalHealthSnapshot.collectAsStateWithLifecycle()
    val activities by viewModel.recommendedActivities.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgAmoled,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BubbleTopAppBar(
                onMenuClick = { viewModel.onRefreshUsageStats() },
                onModelSelect = { /* Model tier selector */ },
                onNewChat = { viewModel.onNewChatClicked() },
                onProfileClick = { /* Profile settings */ },
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            BottomInteractiveStack(
                inputText = uiState.inputText,
                onInputChanged = { viewModel.onInputTextChanged(it) },
                onSend = { viewModel.sendMessage() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Top Section: Digital Health Snapshot Card
            item {
                DigitalHealthCard(
                    snapshot = healthSnapshot,
                    onOpenSettingsClick = { viewModel.onOpenUsageSettings(context) }
                )
            }

            // 2. Middle Section: Recommended Activities from Gemini (Android MCP / AppFunctions)
            item {
                RecommendedActivitiesSection(
                    activities = activities,
                    onToggleActivity = { viewModel.onToggleActivityCompleted(it) },
                    onSimulateMcpClick = { viewModel.simulateGeminiMcpInvocation() }
                )
            }

            // 3. Conversation Section Header / Empty Greeting
            if (uiState.messages.isEmpty()) {
                item {
                    EmptyChatCanvas(
                        userName = uiState.userName,
                        onStarterClick = { starter ->
                            viewModel.onInputTextChanged(starter)
                            viewModel.sendMessage()
                        }
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(GlassWhite06)
                        )
                        Text(
                            text = "CHAT CON BUBBLE AGENT",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                                .background(GlassWhite06)
                        )
                    }
                }

                items(uiState.messages, key = { it.id }) { msg ->
                    when (msg.sender) {
                        MessageSender.USER -> UserMessageBubble(message = msg)
                        MessageSender.BUBBLE -> BubbleAssistantMessage(message = msg)
                    }
                }
                if (uiState.isGenerating) {
                    item {
                        GeneratingIndicator()
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun DigitalHealthCard(
    snapshot: DigitalHealthSnapshot,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, GlassWhite12, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row: Title + Live/Mock chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(BubbleCyan15),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = BubbleCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Tiempo de Pantalla Hoy",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                if (!snapshot.isPermissionGranted) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BubbleCyan10)
                            .border(1.dp, BubbleCyan20, RoundedCornerShape(12.dp))
                            .clickable { onOpenSettingsClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Acceso",
                            color = BubbleCyan,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassWhite05)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "En Vivo (UsageStats)",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }

            // Stats Row: Total vs Recoverable
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${snapshot.totalScreenTimeMinutes / 60}h ${snapshot.totalScreenTimeMinutes % 60}m",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Consumo total",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${snapshot.potentialRecoverableMinutes / 60}h ${snapshot.potentialRecoverableMinutes % 60}m",
                        color = BubbleCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Tiempo potencial recuperable",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }

            // Top apps badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                snapshot.topApps.take(4).forEach { app ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${app.appName} ${app.usageMinutes}m",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedActivitiesSection(
    activities: List<RecommendedActivity>,
    onToggleActivity: (String) -> Unit,
    onSimulateMcpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Sugerencias MCP",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BubbleCyan15)
                    .border(1.dp, BubbleCyan33, RoundedCornerShape(12.dp))
                    .clickable { onSimulateMcpClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = BubbleCyan,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Simular MCP",
                    color = BubbleCyan,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        if (activities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainerLow)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay actividades guardadas. Habla con Bubble o envía una recomendación vía AppFunctions MCP.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            activities.forEach { activity ->
                RecommendedActivityCard(
                    activity = activity,
                    onToggle = { onToggleActivity(activity.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendedActivityCard(
    activity: RecommendedActivity,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLow)
            .border(
                1.dp,
                if (activity.isCompleted) GlassWhite06 else GlassWhite12,
                RoundedCornerShape(16.dp)
            )
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (activity.isCompleted) BubbleCyan else GlassWhite06)
                    .border(
                        1.dp,
                        if (activity.isCompleted) BubbleCyan else GlassWhite16,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (activity.isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = BgAmoled,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BubbleCyan10)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = activity.category,
                            color = BubbleCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "${activity.estimatedMinutes} min",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                Text(
                    text = activity.title,
                    color = if (activity.isCompleted) TextSecondary else TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else null
                )

                if (activity.reason.isNotEmpty()) {
                    Text(
                        text = activity.reason,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

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
fun EmptyChatCanvas(
    userName: String,
    onStarterClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emblem
        Image(
            painter = painterResource(id = R.drawable.logo_bubble_phone),
            contentDescription = "Bubble Logo",
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Greeting Headline
        Text(
            text = "Hola $userName,\n¿qué actividad offline priorizamos hoy?",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Privacy Chip
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BubbleCyan10)
                .border(1.dp, BubbleCyan20, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = BubbleCyan,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Gemini Nano On-Device • 100% Privado",
                color = BubbleCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick starter prompts
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Quiero aprender acordes básicos de guitarra acústica",
                "¿Cómo reduzco mi tiempo en Instagram hoy?",
                "Sugiere una actividad offline de 20 minutos"
            ).forEach { starter ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainerLow)
                        .border(1.dp, GlassWhite06, RoundedCornerShape(12.dp))
                        .clickable { onStarterClick(starter) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = starter,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceContainerHigh)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = OnSurface,
                fontSize = 14.sp,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun BubbleAssistantMessage(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_bubble_phone),
                contentDescription = "Bubble Avatar",
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "Bubble",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(BubbleCyan15)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "LOCAL",
                    color = BubbleCyan,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Text(
            text = message.text,
            color = TextPrimary,
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 21.sp,
            modifier = Modifier.padding(start = 28.dp)
        )

        // Assistant Action Bar (Copy, Retry, Like)
        Row(
            modifier = Modifier
                .padding(start = 28.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistantActionButton(icon = Icons.Rounded.ContentCopy, contentDescription = "Copy") { }
            AssistantActionButton(icon = Icons.Rounded.Refresh, contentDescription = "Retry") { }
            AssistantActionButton(icon = Icons.Rounded.ThumbUp, contentDescription = "Like") { }
        }
    }
}

@Composable
fun AssistantActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(SurfaceContainerLow)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
    }
}

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BottomInteractiveStack(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(bottom = if (isImeVisible) 10.dp else 12.dp)
            .padding(horizontal = 16.dp)
            .then(
                if (!isImeVisible) Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        FloatingPromptBar(
            inputText = inputText,
            onInputChanged = onInputChanged,
            onSend = onSend
        )
    }
}

@Composable
fun FloatingPromptBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(FloatingBarBg)
            .border(1.dp, GlassWhite12, RoundedCornerShape(26.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Plus Button (Attach/Context)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .clickable { /* Attach */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Attach",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Input Field with Prompt Placeholder
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Pregunta a Bubble...",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = onInputChanged,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    ),
                    cursorBrush = SolidColor(BubbleCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Voice / Mic Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GlassWhite06)
                    .clickable { /* Voice Input */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Voice Dictation",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Live / Audio Wave Button or Send Button
            AnimatedVisibility(
                visible = inputText.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BubbleCyan)
                        .clickable { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send Message",
                        tint = BgAmoled,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = inputText.isBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassWhite06)
                        .clickable { /* Audio Live Mode */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = "Live Audio Session",
                        tint = BubbleCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
