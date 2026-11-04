package com.jesusdmedinac.bubble.ai.koog

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.Prompt
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.message.ResponseMetaInfo
import ai.koog.prompt.streaming.StreamFrame
import com.jesusdmedinac.bubble.ai.BubbleAgentManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

object GoogleAICoreLLMProvider : LLMProvider("google-aicore", "Google AICore (Gemini Nano)")

val GeminiNanoModel = LLModel(
    provider = GoogleAICoreLLMProvider,
    id = "gemini-nano"
)

class GeminiNanoKoogClient(
    private val agentManager: BubbleAgentManager
) : LLMClient() {

    override fun llmProvider(): LLMProvider = GoogleAICoreLLMProvider

    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): Message.Assistant {
        val userQuery = prompt.messages
            .lastOrNull { it is Message.User }
            ?.textContent()
            ?: prompt.messages.lastOrNull()?.textContent().orEmpty()

        val fullText = agentManager.streamChat(userQuery).toList().joinToString("")

        return Message.Assistant(
            parts = listOf(MessagePart.Text(fullText)),
            metaInfo = ResponseMetaInfo.Empty
        )
    }

    override fun executeStreaming(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): Flow<StreamFrame> = flow {
        val userQuery = prompt.messages
            .lastOrNull { it is Message.User }
            ?.textContent()
            ?: prompt.messages.lastOrNull()?.textContent().orEmpty()

        agentManager.streamChat(userQuery).collect { chunk ->
            emit(StreamFrame.TextDelta(chunk))
        }
        emit(StreamFrame.End())
    }

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult {
        return ModerationResult(
            isHarmful = false,
            categories = emptyMap()
        )
    }

    override suspend fun models(): List<LLModel> = listOf(GeminiNanoModel)

    override fun close() {
        agentManager.close()
    }
}
