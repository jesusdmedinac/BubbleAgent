package com.jesusdmedinac.bubble.ai.koog

import ai.koog.prompt.Prompt
import ai.koog.prompt.message.Message
import ai.koog.prompt.streaming.StreamFrame
import com.jesusdmedinac.bubble.ai.BubbleAgentManager
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeBubbleAgentManager : BubbleAgentManager(null) {
    var wasClosed = false

    override fun streamChat(userPrompt: String): Flow<String> = flow {
        emit("Hola ")
        emit("desde ")
        emit("Gemini Nano")
    }

    override fun close() {
        wasClosed = true
    }
}

class BubbleKoogAgentTest {

    @Test
    fun geminiNanoKoogClient_propertiesAndModels() = runTest {
        val fakeManager = FakeBubbleAgentManager()
        val client = GeminiNanoKoogClient(fakeManager)

        assertEquals("google-aicore", client.llmProvider().id)
        val models = client.models()
        assertEquals(1, models.size)
        assertEquals("gemini-nano", models.first().id)
        assertEquals(GoogleAICoreLLMProvider, models.first().provider)

        val moderation = client.moderate(Prompt.Empty, GeminiNanoModel)
        assertFalse(moderation.isHarmful)

        client.close()
        assertTrue(fakeManager.wasClosed)
    }

    @Test
    fun geminiNanoKoogClient_execute_returnsAssistantMessage() = runTest {
        val fakeManager = FakeBubbleAgentManager()
        val client = GeminiNanoKoogClient(fakeManager)

        val prompt = Prompt.build("test_prompt") {
            user("Cómo estás?")
        }

        val assistantMessage = client.execute(prompt, GeminiNanoModel)
        assertEquals("Hola desde Gemini Nano", assistantMessage.textContent())
    }

    @Test
    fun geminiNanoKoogClient_executeStreaming_emitsTextDeltasAndEnd() = runTest {
        val fakeManager = FakeBubbleAgentManager()
        val client = GeminiNanoKoogClient(fakeManager)

        val prompt = Prompt.build("test_prompt") {
            user("Ayuda con screen time")
        }

        val frames = client.executeStreaming(prompt, GeminiNanoModel).toList()
        assertEquals(4, frames.size)
        assertTrue(frames[0] is StreamFrame.TextDelta)
        assertEquals("Hola ", (frames[0] as StreamFrame.TextDelta).text)
        assertTrue(frames[1] is StreamFrame.TextDelta)
        assertEquals("desde ", (frames[1] as StreamFrame.TextDelta).text)
        assertTrue(frames[2] is StreamFrame.TextDelta)
        assertEquals("Gemini Nano", (frames[2] as StreamFrame.TextDelta).text)
        assertTrue(frames[3] is StreamFrame.End)
    }

    @Test
    fun bubbleKoogAgent_initializesKoogAgentCorrectly() = runTest {
        val fakeManager = FakeBubbleAgentManager()
        val koogAgent = BubbleKoogAgent(fakeManager)

        assertEquals("bubble-koog-agent", koogAgent.agent.id)
        assertEquals(GeminiNanoModel, koogAgent.agent.agentConfig.model)

        val tokens = koogAgent.streamChat("Hola").toList()
        assertEquals(listOf("Hola ", "desde ", "Gemini Nano"), tokens)

        koogAgent.close()
        assertTrue(fakeManager.wasClosed)
    }
}
