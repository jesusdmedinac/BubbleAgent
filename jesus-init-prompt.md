Actúa como un Arquitecto Senior de Android (Kotlin, Jetpack Compose, Coroutines/StateFlow) especializado en el nuevo Android Intelligence System.

Serás mi copiloto para desarrollar el MVP de Bubble que servirá como aplicación instalada por el usuario con local-first ai.

Vamos a desarrollar el MVP de **Bubble**, una aplicación agéntica enfocada en el bienestar digital. El propósito de Bubble es ayudar al usuario a recuperar su tiempo de pantalla construyendo un perfil de intereses y cosas valiosas que disfrutaría hacer en el mundo real en lugar de hacer scroll infinito.

Antes de escribir código, lee y aplica estrictamente las directivas de tus skills locales instaladas:
- `appfunctions` (para exponer el servidor Android MCP de Bubble hacia Gemini).
- `mlkit-agent-api` y `ml-kit-genai-prompt-api` (para el AI Agent Chat interno de Bubble que entrevista al usuario y construye su perfil de intereses).
- `android-permissions-security` (para el manejo seguro de `PACKAGE_USAGE_STATS`).
- `android-cli` (para verificar compilación, dependencias y estado con Android Studio).

### Arquitectura del Flujo Agéntico (El Ciclo Completo del MVP)

1. **AI Agent Chat en Bubble (Construcción del Perfil de Intereses Offline):**
   - Implementa una interfaz de chat conversacional en Jetpack Compose dentro de Bubble impulsada por `mlkit-agent-api` (`AgentSession` en un ViewModel con manejo de `GenAiException`).
   - El agente conversa con el usuario para descubrir qué actividades reales le apasionan (ej. tocar guitarra acústica, leer, entrenar fuerza, cocinar, caminar en la naturaleza) y qué metas posterga por estar frente a la pantalla.
   - Guarda y actualiza este "Perfil de Vida Offline" (intereses, duración ideal de actividad, energía requerida) en un repositorio local reactivo (`StateFlow` / DataStore o Room).

2. **Integración con Screen Time API de Android (`UsageStatsManager`):**
   - Crea un `ScreenTimeTracker` que consulte `UsageStatsManager` para obtener las métricas de tiempo en pantalla del día actual (tiempo total, top apps consumidoras de atención y rachas de uso continuo).
   - Incluye un modo *Fallback / Demo Mock* con datos realistas pre-cargados en caso de que el permiso `PACKAGE_USAGE_STATS` aún no esté concedido durante la demo en vivo, más el botón directo a `Settings.ACTION_USAGE_ACCESS_SETTINGS`.

3. **Servidor Android MCP de Bubble para Gemini (`@AppFunctionService` / `appfunctions`):**
   Expón tres capacidades bidireccionales mediante `androidx.appfunctions` con KDocs semánticos impecables para que Gemini (y el Android Intelligence System) puedan invocarlas sin abrir la UI de Bubble:
   - `getDigitalHealthSnapshot()`: Comparte de forma segura con Gemini el resumen de tiempo en pantalla actual + el perfil de intereses/gustos del usuario registrado en Bubble.
   - `getUserOfflineInterests()`: Devuelve la lista estructurada de pasatiempos y metas offline que el usuario quiere priorizar.
   - `saveRecommendedActivity(title: String, category: String, estimatedMinutes: Int, reason: String)`: Permite que Gemini (usando su inteligencia personal y el snapshot de salud digital) guarde directamente en Bubble actividades optimizadas y accionables para reemplazar el tiempo en pantalla.

4. **UI del MVP en Jetpack Compose (Una sola pantalla o navegación ultra limpia):**
   - **Sección Superior (Salud Digital):** Tarjeta minimalista mostrando el tiempo en pantalla actual vs. tiempo potencial recuperable.
   - **Sección Central (Actividades Sugeridas por Gemini vía MCP):** Lista reactiva que se actualiza en tiempo real cuando Gemini invoca `saveRecommendedActivity(...)` a través de AppFunctions.
   - **Sección Inferior / Pestaña (Bubble Agent Chat):** El chat con el agente local para seguir enriqueciendo el perfil de gustos del usuario, más un botón de *"Simular invocación MCP de Gemini"* (o comando ADB listo) para demostrar el flujo end-to-end en vivo.

### Entregables del Primer Paso
1. Revisa la estructura del proyecto actual y configura las dependencias necesarias en `libs.versions.toml` y `build.gradle.kts` (`androidx.appfunctions`, `com.google.mlkit:genai-agent`, Compose Material 3).
2. Implementa el modelo de datos y el repositorio reactivo en memoria/DataStore que conecta la UI con el `AppFunctionService`.
3. Implementa el servicio MCP (`BubbleMcpAppFunctionService`) con sus tres funciones expuestas y su registro en `AndroidManifest.xml`.
4. Implementa el `BubbleAgentViewModel` y la pantalla principal en Compose.
5. Dame los comandos exactos de `adb` (según el skill `appfunctions`) para probar la llamada de lectura y escritura de Gemini hacia Bubble en vivo.