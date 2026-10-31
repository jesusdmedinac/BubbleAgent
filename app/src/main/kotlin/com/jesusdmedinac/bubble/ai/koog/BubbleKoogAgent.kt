package com.jesusdmedinac.bubble.ai.koog

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.Prompt
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.streaming.StreamFrame
import android.content.Context
import com.jesusdmedinac.bubble.ai.BubbleAgentManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BubbleKoogAgent(
    private val agentManager: BubbleAgentManager
) : AutoCloseable {

    constructor(context: Context) : this(BubbleAgentManager(context.applicationContext))

    companion object {
        const val SYSTEM_INSTRUCTIONS: String = """
            You are Bubble, an empathetic on-device digital wellbeing assistant running privately on Android via Gemini Nano through Koog.
            Your purpose is to help the user recover screen time by understanding what they genuinely enjoy doing in the physical world and maintaining their personal vault profile.
            You have access to on-device AppFunctions tools:
            - readUserProfile: Retrieve today's screen time, top apps, offline interests, goals, and vault memories.
            - saveUserProfile: Update the user's vault profile with new insights, habits, offline interests, or goals learned during conversations.
            When the user asks about their screen time, habits, or profile, call readUserProfile to provide accurate on-device information.
            When you learn something new and valuable about the user's offline interests, habits, or goals, call saveUserProfile to store it in their Bubble vault.
            Keep answers concise, inspiring, and focused on helping them disconnect from doomscrolling and reconnect with real life.
            Always respond in the language used by the user (Spanish by default).
        """
    }

    private val koogClient = GeminiNanoKoogClient(agentManager)

    val promptExecutor: PromptExecutor = MultiLLMPromptExecutor(listOf(koogClient))

    val agent: AIAgent<String, String> = AIAgent.builder()
        .id("bubble-koog-agent")
        .promptExecutor(promptExecutor)
        .llmModel(GeminiNanoModel)
        .systemPrompt(SYSTEM_INSTRUCTIONS.trimIndent())
        .build()

    fun streamChat(userPrompt: String): Flow<String> = flow {
        val prompt = Prompt.build("bubble_chat_session") {
            system(SYSTEM_INSTRUCTIONS.trimIndent())
            user(userPrompt)
        }

        promptExecutor.executeStreaming(prompt, GeminiNanoModel).collect { frame ->
            if (frame is StreamFrame.TextDelta) {
                emit(frame.text)
            }
        }
    }

    suspend fun run(userPrompt: String): String {
        return agent.run(userPrompt)
    }

    suspend fun closeAsync() {
        try {
            agent.close()
        } catch (ignored: Exception) {}
        try {
            promptExecutor.close()
        } catch (ignored: Exception) {}
    }

    override fun close() {
        try {
            promptExecutor.close()
        } catch (ignored: Exception) {}
    }
}
