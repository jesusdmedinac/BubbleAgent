package com.jesusdmedinac.bubble.ai

import android.content.Context
import com.jesusdmedinac.bubble.ai.koog.BubbleKoogAgent
import kotlinx.coroutines.flow.Flow

/**
 * Koog-powered AI Engine: abstracts on-device Gemini Nano using JetBrains Koog framework
 * (AIAgent and PromptExecutor architecture).
 */
class KoogGeminiNanoAiEngine(
    context: Context
) : BubbleAiEngine {
    private val koogAgent = BubbleKoogAgent(context.applicationContext)

    override fun streamChat(userPrompt: String): Flow<String> {
        return koogAgent.streamChat(userPrompt)
    }

    override fun close() {
        koogAgent.close()
    }
}

class GeminiNanoAiEngine(
    context: Context
) : BubbleAiEngine {
    private val delegate = KoogGeminiNanoAiEngine(context)

    override fun streamChat(userPrompt: String): Flow<String> {
        return delegate.streamChat(userPrompt)
    }

    override fun close() {
        delegate.close()
    }
}
