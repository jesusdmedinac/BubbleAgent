package com.jesusdmedinac.bubble.data.model

import com.jesusdmedinac.bubble.core.currentTimeMillis
import com.jesusdmedinac.bubble.core.randomId
import com.jesusdmedinac.jsontocompose.model.ComposeDocument

enum class MessageSender {
    USER, BUBBLE
}

data class ChatMessage(
    val id: String = randomId(),
    val conversationId: String = "",
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = currentTimeMillis(),
    val sduiJson: String? = null,
    val dynamicDocument: ComposeDocument? = null
)
