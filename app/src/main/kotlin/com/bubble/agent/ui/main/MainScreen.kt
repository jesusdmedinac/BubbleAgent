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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bubble.agent.R
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

import androidx.compose.ui.platform.LocalContext
import com.bubble.agent.ai.GeminiNanoAiEngine

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    context: android.content.Context = LocalContext.current.applicationContext,
    viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(GeminiNanoAiEngine(context))
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BgAmoled,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            BubbleTopAppBar(
                onMenuClick = { /* Drawer / menu action */ },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.messages.isEmpty()) {
                EmptyChatCanvas(userName = uiState.userName)
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
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
                    item { Spacer(modifier = Modifier.height(16.dp)) }
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large Emblem (68x68 circle)
        Image(
            painter = painterResource(id = R.drawable.logo_bubble_phone),
            contentDescription = "Bubble Logo",
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Greeting Headline
        Text(
            text = "Hi $userName,\nwhat's on your mind?",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            textAlign = TextAlign.Center,
            lineHeight = 31.sp
        )

        Spacer(modifier = Modifier.height(18.dp))

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
                text = "Local First • 100% Private chat",
                color = BubbleCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            )
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
fun GeneratingIndicator() {
    Row(
        modifier = Modifier.padding(start = 28.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(BubbleCyan)
        )
        Text(
            text = "Bubble local thinking...",
            color = BubbleCyan,
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
    val density = LocalDensity.current
    val isKeyboardOpen = WindowInsets.ime.getBottom(density) > 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = if (isKeyboardOpen) 10.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating AI Prompt Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(FloatingBarBg)
                .border(1.dp, GlassWhite16, RoundedCornerShape(32.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Plus Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { /* Attach */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add attachment",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Input Text Field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Ask Bubble...",
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSend() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right Group: Mic & Live AI Call or Send Button
            AnimatedVisibility(
                visible = inputText.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BubbleCyan)
                        .clickable { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send Message",
                        tint = BgAmoled,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = inputText.isBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { /* Voice input */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Voice Input",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BubbleCyan15)
                            .border(1.dp, BubbleCyan33, CircleShape)
                            .clickable { /* Live AI Call */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq,
                            contentDescription = "Live AI Call",
                            tint = BubbleCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (!isKeyboardOpen) {
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MainScreenPreview() {
    MainScreen()
}
