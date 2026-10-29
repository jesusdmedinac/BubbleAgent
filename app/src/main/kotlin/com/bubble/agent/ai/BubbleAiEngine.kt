package com.bubble.agent.ai

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface BubbleAiEngine {
    fun streamChat(userPrompt: String): Flow<String>
    fun close()
}

class GeminiNanoAiEngine(
    context: Context
) : BubbleAiEngine {
    private val agentManager = BubbleAgentManager(context.applicationContext)

    override fun streamChat(userPrompt: String): Flow<String> {
        return agentManager.streamChat(userPrompt)
    }

    override fun close() {
        agentManager.close()
    }
}

class MockAiEngine : BubbleAiEngine {
    override fun streamChat(userPrompt: String): Flow<String> = flow {
        delay(100)
        val lower = userPrompt.lowercase()
        val response = when {
            lower.contains("guitarra") || lower.contains("musica") ->
                "¡Excelente elección! La práctica de guitarra estimula la concentración profunda y la motricidad fina sin fatiga digital. Guardando 'Práctica de guitarra acústica (30 min)' en tu Perfil Offline."
            lower.contains("tiempo") || lower.contains("scroll") || lower.contains("pantalla") ->
                "He notado que las tardes suelen ser el momento con mayor fricción y consumo pasivo. ¿Qué tal si dedicamos los próximos 30 minutos a tocar guitarra o caminar un rato?"
            lower.contains("hola") || lower.contains("hi") || lower.contains("hey") ->
                "¡Hola Jesús! Estoy aquí en tu dispositivo de forma 100% privada y local vía Gemini Nano. ¿Qué actividad del mundo real te gustaría rescatar hoy en lugar de estar en pantalla?"
            else ->
                "Entendido. Como tu copiloto local de Bubble, mi misión es ayudarte a cambiar el scroll por experiencias reales. ¿Te gustaría agendar esta actividad en tus prioridades offline de hoy?"
        }
        for (chunk in response.split(" ")) {
            emit("$chunk ")
            delay(30)
        }
    }

    override fun close() {}
}
