package com.bubble.agent.data.model

/**
 * Represents the user's offline life profile, storing genuine real-world interests,
 * hobbies, and preferred activity constraints to counter screen time addiction.
 */
data class OfflineProfile(
    val interests: List<String> = listOf(
        "Tocar guitarra acústica",
        "Senderismo en la naturaleza",
        "Lectura de ciencia ficción",
        "Entrenamiento de fuerza"
    ),
    val idealDurationMinutes: Int = 30,
    val energyLevel: String = "MEDIUM",
    val goals: List<String> = listOf(
        "Aprender canciones completas en guitarra",
        "Reducir scroll nocturno a 0 minutos"
    )
)
