package com.jesusdmedinac.bubble.ai.vault

import com.jesusdmedinac.bubble.data.model.VaultCategory
import java.util.UUID

data class VaultMemoryCandidate(
    val id: String = UUID.randomUUID().toString(),
    val tag: String,
    val category: VaultCategory,
    val title: String,
    val source: String = "Conversación en Bubble",
    val footerDetail: String = "Detectado por Gemini Nano",
    val tagColorHex: Long = 0xFF00D2D3
)

object VaultMemoryDetector {

    /**
     * Inspects user prompt and assistant output to identify candidate memories
     * that should be dynamically proposed to the user for Vault persistence.
     */
    fun detectCandidate(userText: String, assistantReply: String): VaultMemoryCandidate? {
        val lowerUser = userText.lowercase()

        // 1. Music & Creative Practice
        if (lowerUser.contains("guitarra") || lowerUser.contains("música") || lowerUser.contains("musica") || lowerUser.contains("piano") || lowerUser.contains("batería") || lowerUser.contains("bateria")) {
            val instrument = when {
                lowerUser.contains("guitarra") -> "guitarra acústica"
                lowerUser.contains("piano") -> "piano"
                lowerUser.contains("batería") || lowerUser.contains("bateria") -> "batería"
                else -> "música"
            }
            return VaultMemoryCandidate(
                tag = "MÚSICA",
                category = VaultCategory.PREFERENCE,
                title = "Interés en práctica musical ($instrument)",
                source = "Conversación en Bubble",
                footerDetail = "Preferencia offline detectada",
                tagColorHex = 0xFF00D2D3
            )
        }

        // 2. Reading & Book Habits
        if (lowerUser.contains("leer") || lowerUser.contains("lectura") || lowerUser.contains("libro")) {
            return VaultMemoryCandidate(
                tag = "LECTURA",
                category = VaultCategory.MCP_HABIT,
                title = "Hábito de lectura de libros sin pantallas",
                source = "Conversación en Bubble",
                footerDetail = "Hábito offline activo",
                tagColorHex = 0xFFFFD700
            )
        }

        // 3. Physical Sports & Nature Outdoors
        if (lowerUser.contains("correr") || lowerUser.contains("caminar") || lowerUser.contains("caminata") ||
            lowerUser.contains("ejercicio") || lowerUser.contains("gym") || lowerUser.contains("gimnasio") ||
            lowerUser.contains("yoga") || lowerUser.contains("bicicleta") || lowerUser.contains("nadar")
        ) {
            val activity = when {
                lowerUser.contains("correr") -> "running y trote"
                lowerUser.contains("caminar") || lowerUser.contains("caminata") -> "caminata al aire libre"
                lowerUser.contains("gym") || lowerUser.contains("gimnasio") -> "entrenamiento de fuerza"
                lowerUser.contains("yoga") -> "yoga y estiramiento"
                lowerUser.contains("bicicleta") -> "ciclismo"
                else -> "actividad física"
            }
            return VaultMemoryCandidate(
                tag = "DEPORTE",
                category = VaultCategory.MCP_HABIT,
                title = "Reconexión física activa ($activity)",
                source = "Conversación en Bubble",
                footerDetail = "Hábito saludable registrado",
                tagColorHex = 0xFF4CD964
            )
        }

        // 4. Intentional Screen Time Limits & Boundaries
        if (lowerUser.contains("meta") || lowerUser.contains("reducir") || lowerUser.contains("limitar") ||
            lowerUser.contains("desconectar") || lowerUser.contains("dejar de usar") || lowerUser.contains("bloquear")
        ) {
            val limitTarget = when {
                lowerUser.contains("redes") || lowerUser.contains("instagram") || lowerUser.contains("tiktok") -> "en redes sociales"
                lowerUser.contains("30 minutos") || lowerUser.contains("30 min") -> "de 30 minutos de pantalla"
                lowerUser.contains("noche") || lowerUser.contains("dormir") -> "antes de dormir"
                else -> "de tiempo de pantalla"
            }
            return VaultMemoryCandidate(
                tag = "LÍMITE",
                category = VaultCategory.OFFLINE_GOAL,
                title = "Meta de desconexión $limitTarget",
                source = "Meta acordada con Bubble",
                footerDetail = "Límite proactivo en Vault",
                tagColorHex = 0xFFFF9F43
            )
        }

        // 5. Schedules & Night Rest
        if (lowerUser.contains("noche") || lowerUser.contains("insomnio") || lowerUser.contains("sueño") || lowerUser.contains("dormir")) {
            return VaultMemoryCandidate(
                tag = "SUEÑO",
                category = VaultCategory.MCP_HABIT,
                title = "Rutina de desconexión nocturna para descansar mejor",
                source = "Conversación en Bubble",
                footerDetail = "Horario sensible de descanso",
                tagColorHex = 0xFF54A0FF
            )
        }

        // 6. Generic Preference ("me gusta", "me encanta", "disfruto", "mi pasión")
        if (lowerUser.contains("me gusta") || lowerUser.contains("me encanta") || lowerUser.contains("mi pasión")) {
            val cleanTitle = userText
                .replace(Regex("(?i)^(hola,?\\s*|oye,?\\s*|bueno,?\\s*)"), "")
                .take(50)
            return VaultMemoryCandidate(
                tag = "PREFERENCIA",
                category = VaultCategory.PREFERENCE,
                title = "Preferencia: $cleanTitle",
                source = "Conversación en Bubble",
                footerDetail = "Gusto personal detectado",
                tagColorHex = 0xFF00D2D3
            )
        }

        return null
    }
}
