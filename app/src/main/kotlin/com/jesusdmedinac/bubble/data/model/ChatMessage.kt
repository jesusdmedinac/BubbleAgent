package com.jesusdmedinac.bubble.data.model

import java.util.UUID

enum class MessageSender {
    USER, BUBBLE
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
