package com.bubble.agent.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bubble.agent.ui.theme.BubbleTheme

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
@Preview
fun BottomInteractiveStackPreview() {
    BubbleTheme {
        BottomInteractiveStack(
            inputText = "",
            onInputChanged = {},
            onSend = {}
        )
    }
}
