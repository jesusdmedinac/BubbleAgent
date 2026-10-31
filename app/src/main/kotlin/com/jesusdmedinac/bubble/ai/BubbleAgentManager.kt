package com.jesusdmedinac.bubble.ai

import android.content.Context
import android.util.Log
import com.google.mlkit.genai.agent.Agent
import com.google.mlkit.genai.agent.AgentRunner
import com.google.mlkit.genai.agent.AgentSession
import com.google.mlkit.genai.agent.textTokens
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Content
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import com.google.mlkit.genai.prompt.ModelPreference
import com.google.mlkit.genai.prompt.ModelReleaseStage
import com.google.mlkit.genai.prompt.generationConfig
import com.google.mlkit.genai.prompt.modelConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BubbleAgentManager(private val context: Context) {

    private val systemInstructions = """
        You are Bubble, an empathetic on-device digital wellbeing assistant running privately on Android via Gemini Nano.
        Your purpose is to help the user recover screen time by discovering what they genuinely enjoy doing in the physical world.
        Ask engaging, thoughtful questions about their passions, hobbies, offline aspirations, and creative pursuits (such as playing acoustic guitar, outdoor walks, reading books, cooking, woodworking, fitness).
        Keep answers concise, inspiring, and focused on helping them disconnect from doomscrolling and reconnect with real life.
        Always respond in the language used by the user (Spanish by default).
    """.trimIndent()

    private val generativeModel: GenerativeModel by lazy {
        try {
            val config = generationConfig {
                modelConfig = modelConfig {
                    releaseStage = ModelReleaseStage.PREVIEW
                    preference = ModelPreference.FAST
                }
            }
            Log.d("BubbleAgent", "Initializing GenerativeModel with PREVIEW FAST (Galaxy S24 AICore)...")
            Generation.getClient(config)
        } catch (e: Exception) {
            Log.e("BubbleAgent", "Falling back to default GenerativeModel: ${e.message}")
            Generation.getClient()
        }
    }

    private val agent: Agent by lazy {
        Agent.builder()
            .setName("Bubble")
            .setInstructions(systemInstructions)
            .build()
    }

    private val runner: AgentRunner by lazy {
        try {
            AgentRunner(context, agent, generativeModel)
        } catch (e: Exception) {
            Log.e("BubbleAgent", "Failed to initialize AgentRunner with custom model, using default: ${e.message}")
            AgentRunner(agent)
        }
    }

    private val session: AgentSession by lazy {
        runner.getOrCreateSession("bubble_interview_session")
    }

    fun streamChat(userPrompt: String): Flow<String> = flow {
        // Attempt 1: AgentRunner with session
        var streamedTokens = 0
        try {
            val content = Content.builder().text(userPrompt).build()
            session.runAsync(content)
                .textTokens()
                .collect { token ->
                    streamedTokens++
                    emit(token)
                }
            return@flow
        } catch (e: Throwable) {
            Log.w("BubbleAgent", "AgentRunner session failed (${e.message}). Falling back directly to Gemini Nano GenerativeModel streaming...", e)
        }

        // Attempt 2: Direct Gemini Nano GenerativeModel streaming on S24 Ultra NPU
        try {
            val promptWithContext = "Instrucciones de sistema:\n$systemInstructions\n\nUsuario: $userPrompt\nBubble:"
            generativeModel.generateContentStream(promptWithContext)
                .collect { response ->
                    response.candidates.forEach { candidate ->
                        val text = candidate.text
                        if (!text.isNullOrEmpty()) {
                            streamedTokens++
                            emit(text)
                        }
                    }
                }
            if (streamedTokens > 0) return@flow
        } catch (e: Throwable) {
            Log.e("BubbleAgent", "Direct Gemini Nano GenerativeModel streaming failed: ${e.message}", e)
        }

        // Attempt 3: Direct single-turn generateContent on Gemini Nano
        try {
            val promptWithContext = "Instrucciones de sistema:\n$systemInstructions\n\nUsuario: $userPrompt\nBubble:"
            val response = generativeModel.generateContent(promptWithContext)
            val text = response.candidates.firstOrNull()?.text
            if (!text.isNullOrEmpty()) {
                emit(text)
                return@flow
            }
        } catch (e: Throwable) {
            Log.e("BubbleAgent", "Direct Gemini Nano generateContent failed: ${e.message}", e)
            emit("Error de Gemini Nano local: ${e.localizedMessage ?: e.message}")
        }
    }

    fun close() {
        try {
            session.close()
        } catch (ignored: Exception) {}
        try {
            generativeModel.close()
        } catch (ignored: Exception) {}
    }
}
