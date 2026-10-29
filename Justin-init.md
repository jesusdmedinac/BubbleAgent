Antes de empezar extrae el contenido del archivo all-skills.zip y lee todas las skills y selecciona cuales pueden ser de ayuda para el proyecto

Fase 1: Configuración del Proyecto y Dependencias (AppFunctions & KSP)
Para exponer tu servicio local hacia Gemini, el SDK de Jetpack requiere el uso de KSP (Kotlin Symbol Processing) para generar los esquemas en tiempo de compilación.

> ⚠️ IMPORTANTE: Verifica que `appfunctions` exista en los repositorios de Maven/Google para tu versión de Android SDK.
> Si estás en Android 16 developer preview, asegúrate de tener el SDK target correcto (API level 36+).

En tu libs.versions.toml:
[versions]
appfunctions = "1.0.0-alpha01" # Verifica la última versión en developer.android.com
ksp = "2.0.0-1.0.24" # Asegúrate de que coincida con tu versión de Kotlin

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
androidx-appfunctions-common = { group = "androidx.appfunctions", name = "appfunctions-common", version.ref = "appfunctions" }
androidx-appfunctions-compiler = { group = "androidx.appfunctions", name = "appfunctions-compiler", version.ref = "appfunctions" }

En el build.gradle.kts del módulo app:
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp) // Habilitar KSP via version catalog
}

dependencies {
    implementation(libs.androidx.appfunctions.common)
    ksp(libs.androidx.appfunctions.compiler)
    // Compose y otras dependencias MVVM...
}

Fase 2: Modelado de Datos MCP (El Contrato Semántico)
Según las especificaciones de Connected Apps y AppFunctions, cualquier objeto complejo que pases o devuelvas debe estar estrictamente tipado y anotado para que el compilador genere el esquema JSON que el LLM consumirá.
Crea tus modelos de dominio. Para un MVP rápido y robusto, inyectaremos datos mockeados con actividades genéricas y universales que cualquier usuario pueda identificar, para demostrar cómo el LLM personaliza sus recomendaciones basándose en los intereses del perfil.
import androidx.appfunctions.AppFunctionSerializable

@AppFunctionSerializable
data class DigitalHealthSnapshot(
    val totalScreenTimeMinutes: Int,
    val continuousScrollMinutes: Int,
    val topDistractingApp: String,
    val offlineInterests: List<String>,
    val transportationMethod: String
)

@AppFunctionSerializable
data class SaveActivityResult(
    val success: Boolean,
    val message: String,
    val savedTitle: String
)

Fase 3: Implementación del Servicio AppFunctions
Esta es la capa donde tu Clean Architecture brilla. El servicio actúa como un controlador de entrada que delega la lógica a tus Use Cases o Repositorios. La clave de las buenas prácticas de developer.android.com/ai/appfunctions es el KDoc: El KDoc es tu System Prompt para esta herramienta.
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService

class BubbleMcpAppFunctionService : AppFunctionService() {

    // Simulación de repositorio para el Hackathon
    private fun getMockSnapshot() = DigitalHealthSnapshot(
        totalScreenTimeMinutes = 180,
        continuousScrollMinutes = 35, // Gatillo para intervención
        topDistractingApp = "TikTok",
        offlineInterests = listOf(
            "Salir a caminar al aire libre",
            "Leer un libro físico",
            "Cocinar una receta nueva",
            "Hacer ejercicio o estiramientos"
        ),
        transportationMethod = "Caminando"
    )

    /**
     * Retrieves the user's current digital health snapshot.
     * Call this function to understand the user's current screen time accumulation 
     * and their specific offline interests BEFORE generating a real-world activity recommendation.
     */
    @AppFunction
    fun getDigitalHealthSnapshot(): DigitalHealthSnapshot {
        // En producción: return digitalHealthUseCase.invoke()
        return getMockSnapshot()
    }

    /**
     * Saves a successfully accepted real-world activity recommendation into the user's profile.
     * ONLY call this function if the user explicitly agrees to do the recommended activity.
     *
     * @param title A short, actionable title for the activity. Must not be empty.
     * @param category The category (e.g., "Wellness", "Exercise", "Hobby").
     * @param feedbackScore A rating from 1 to 5 indicating user enthusiasm.
     * @return A SaveActivityResult indicating whether the save was successful and a confirmation message.
     */
    @AppFunction
    fun saveRecommendedActivity(
        title: String, 
        category: String, 
        feedbackScore: Int
    ): SaveActivityResult {
        // Validación básica de inputs
        require(title.isNotBlank()) { "El título no puede estar vacío" }
        require(feedbackScore in 1..5) { "feedbackScore debe estar entre 1 y 5" }

        // En producción: recommendationUseCase.save(title, category, feedbackScore)
        println("Guardado exitoso desde Gemini: $title [$category] Score: $feedbackScore")
        return SaveActivityResult(
            success = true,
            message = "¡Actividad '$title' guardada! Gemini recordará esta preferencia.",
            savedTitle = title
        )
    }
}

Fase 4: Exposición en el AndroidManifest.xml
El sistema Android Intelligence (y Gemini) solo podrá descubrir tus funciones si el servicio está expuesto correctamente con los permisos adecuados.
<service
    android:name=".BubbleMcpAppFunctionService"
    android:exported="true"
    android:permission="android.permission.BIND_APP_FUNCTION_SERVICE">
    <intent-filter>
        <action android:name="androidx.appfunctions.action.EXECUTE_APP_FUNCTION" />
    </intent-filter>
</service>

Fase 5: Pruebas Locales (El Flujo del Repositorio de Ejemplo)
Antes de construir toda la UI de Jetpack Compose, puedes garantizar que tu integración MCP funciona ejecutando comandos ADB desde tu terminal. Esto es vital en un hackathon para validar el backend local.
1. Compila la app: Asegúrate de que KSP genere las clases (haz un Rebuild Project en Android Studio).
2. Prueba la función de lectura: Ejecuta esto en tu terminal para simular la petición de Gemini hacia Bubble:
adb shell am start-foreground-service \
    -a androidx.appfunctions.action.EXECUTE_APP_FUNCTION \
    -n com.tu.paquete/.BubbleMcpAppFunctionService \
    --es "androidx.appfunctions.extra.FUNCTION_ID" "com.tu.paquete.BubbleMcpAppFunctionService.getDigitalHealthSnapshot"

1. Prueba la función de escritura (con parámetros): Para enviar la recomendación de vuelta desde Gemini usando un payload JSON serializado:
adb shell am start-foreground-service \
    -a androidx.appfunctions.action.EXECUTE_APP_FUNCTION \
    -n com.tu.paquete/.BubbleMcpAppFunctionService \
    --es "androidx.appfunctions.extra.FUNCTION_ID" "com.tu.paquete.BubbleMcpAppFunctionService.saveRecommendedActivity" \
    --es "androidx.appfunctions.extra.FUNCTION_PARAMETERS" '{"title":"Salir a caminar 15 minutos por el parque", "category":"Wellness", "feedbackScore":5}'

Fase 6: Capa de Presentación (Jetpack Compose + MVVM)
Con el backend MCP validado, construir la UI que represente la identidad de Bubble.
Componentes clave:
1. BubbleScreen (Composable principal): Pantalla con el estado de bienestar digital del usuario.
   - Indicador visual de tiempo de pantalla (progreso circular o barra).
   - Lista de intereses offline del usuario.
   - Card con la última recomendación aceptada.
2. BubbleViewModel: Orquesta los Use Cases y expone StateFlow al Composable.
   - Estado: DigitalHealthSnapshot, lista de actividades guardadas, estado de carga.
3. Notificación/Bubble flotante (stretch goal): Si el tiempo de scroll continuo supera el umbral (ej. 30 min), mostrar una notificación o burbuja flotante sugiriendo desconectar.

> 💡 Para el hackathon: Prioriza que la UI muestre los datos mock y que la integración con Gemini via AppFunctions sea la estrella de la demo. La UI puede ser simple si el flujo end-to-end funciona.

Fase 7: Estrategia de Demo y Fallback
> ⚠️ RIESGO: La demo depende de Android 16 + Gemini on-device. No todos los jueces/dispositivos tendrán soporte.

Estrategia de mitigación:
1. Grabar un video de la demo funcional como respaldo.
2. Preparar un emulador Android 16 preconfigurado (API 36+).
3. Si Gemini on-device no está disponible, considerar un fallback con la Gemini API remota para simular el flujo conversacional.
4. Tener capturas de pantalla del flujo completo en la presentación.