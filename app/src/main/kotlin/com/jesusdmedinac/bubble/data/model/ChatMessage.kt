package com.jesusdmedinac.bubble.data.model

import com.jesusdmedinac.jsontocompose.model.ComposeDocument
import java.util.UUID

enum class MessageSender {
    USER, BUBBLE
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sduiJson: String? = null,
    val dynamicDocument: ComposeDocument? = null
)
