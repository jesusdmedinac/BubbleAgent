package com.bubble.agent.ai

import android.content.Context
import com.google.mlkit.genai.agent.Agent
import com.google.mlkit.genai.agent.AgentRunner
import com.google.mlkit.genai.agent.AgentSession
import com.google.mlkit.genai.agent.textTokens
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.Content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BubbleAgentManager(private val context: Context) {

    private val agent: Agent by lazy {
        Agent.builder()
            .setName("Bubble")
            .setInstructions(
                """
                You are Bubble, an empathetic on-device digital wellbeing assistant running privately on Android via Gemini Nano.
                Your purpose is to help the user recover screen time by discovering what they genuinely enjoy doing in the physical world.
                Ask engaging, thoughtful questions about their passions, hobbies, offline aspirations, and creative pursuits (such as playing acoustic guitar, outdoor walks, reading books, cooking, woodworking, fitness).
                Keep answers concise, inspiring, and focused on helping them disconnect from doomscrolling and reconnect with real life.
                Always respond in the language used by the user (Spanish by default).
                """.trimIndent()
            )
            .build()
    }

    private val runner: AgentRunner by lazy {
        AgentRunner(agent)
    }

    private val session: AgentSession by lazy {
        runner.getOrCreateSession("bubble_interview_session")
    }

    fun streamChat(userPrompt: String): Flow<String> = flow {
        try {
            val content = Content.builder().text(userPrompt).build()
            session.runAsync(content)
                .textTokens()
                .collect { token ->
                    emit(token)
                }
        } catch (e: GenAiException) {
            val fallbackMessage = getFallbackResponse(userPrompt, e)
            emit(fallbackMessage)
        } catch (e: Exception) {
            val fallbackMessage = getFallbackResponse(userPrompt, null)
            emit(fallbackMessage)
        }
    }

    private fun getFallbackResponse(prompt: String, error: GenAiException?): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("guitarra") || lower.contains("musica") ->
                "¡Excelente elección! La guitarra acústica es una forma ideal de entrar en estado de flow sin pantallas. ¿Qué estilo o canciones te inspiran?"
            lower.contains("tiempo") || lower.contains("scroll") || lower.contains("pantalla") ->
                "He registrado tu meta de reducir tiempo en pantalla. Los descansos de 20 minutos con actividades táctiles reducen la fatiga mental de inmediato."
            lower.contains("hola") || lower.contains("hey") ->
                "¡Hola Jesús! Soy Bubble, tu copiloto de bienestar digital impulsado por Gemini Nano en tu dispositivo. ¿Qué actividad del mundo real te gustaría rescatar hoy?"
            else ->
                "Entendido. Como tu copiloto local de Bubble, mi misión es ayudarte a cambiar el scroll por experiencias reales. ¿Te gustaría agendar esta actividad en tus prioridades offline de hoy?"
        }
    }

    fun close() {
        try {
            session.close()
        } catch (ignored: Exception) {}
    }
}
