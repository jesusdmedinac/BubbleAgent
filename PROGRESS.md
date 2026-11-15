# Bubble - Plan de Avance y Publicación (`com.jesusdmedinac.bubble`)

## Fase 1: Compilación, Versión y Firma Criptográfica (Release)
- [x] 1.1 Incrementar `versionCode = 18` y `versionName = "1.1.0"` en `BubbleAgent/app/build.gradle.kts`. (Actualizado a `versionCode = 20`, `versionName = "1.2.1"` para lanzamiento en Google Play).
- [x] 1.2 Ajustar `targetSdk = 36` (y `compileSdk = 37` por compatibilidad de AppFunctions) para cumplir con las directivas de Google Play 2026.
- [x] 1.3 Configurar `signingConfigs.release` en `BubbleAgent/app/build.gradle.kts` con `bubble-keystore`.
- [x] 1.4 Crear `BubbleAgent/app/proguard-rules.pro` con reglas R8 para Compose, SQLDelight, Koog, AppFunctions y Serialization.
- [x] 1.5 Validar compilación de `app-release.aab` y `app-release.apk` con `versionCode = 18`, y verificar su firma con `jarsigner -verify` y `apksigner`.
- [x] 1.6 Resolver error bloqueante de Google Play Console: Configurar Declaración de ID de Publicidad en "No" en Contenido de la app.

### Registro de Advertencias y Optimizaciones Técnicas (Google Play Console):
- [ ] **Opt.1 (Desofuscación / R8):** La consola advierte falta de `mapping.txt`. Actualmente `isMinifyEnabled = false` para garantizar estabilidad de Gemini Nano y Koog en pruebas iniciales. Tarea futura: Habilitar R8 con `proguard-rules.pro` y generar archivo de mapeo para producción final.
- [ ] **Opt.2 (Símbolos Nativo C++):** La consola advierte código nativo sin símbolos de depuración (`.so` de SQLite/SQLDelight y NPU). Tarea futura: Agregar `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` en `buildTypes.release` dentro de `app/build.gradle.kts` para empaquetar símbolos automáticamente.

## Fase 2: Interactividad de la UI y Eliminación de Stubs
- [x] 2.1 Implementar acciones en `BubbleAssistantMessage.kt` vía `BubbleAssistantActionBar.kt` (6 micro-acciones containerless: Copy animado con LocalClipboard, Audio TTS, Like, Dislike, Share Sheet nativo y Más opciones).
- [x] 2.2 Cablear botones de `FloatingPromptBar.kt` (Botón `+` para contexto vía `AttachContextBottomSheet` y Botón `Mic` para dictado por voz nativo vía `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`).
- [x] 2.3 Cablear botón de audio en vivo en `LiveAudioWaveOrSendButton.kt` con notificación de disponibilidad futura de AICore Live.
- [x] 2.4 Cablear selector `Bubble Local` en `BubbleTopAppBar.kt` y `MainScreen.kt` con `BubbleLocalInfoBottomSheet` informativo sobre Gemini Nano On-Device.
- [x] 2.5 Añadir retroalimentación visual (Snackbar) a las opciones "Coming Soon" (`Focus mode`, `Apps`, `Challenges`) en `BubbleMenuDrawer.kt`.
- [x] 2.6 Dotar de interactividad informativa al escudo de seguridad en `ProfileVaultTopBar.kt` mediante `VaultSecurityInfoDialog.kt`.
- [x] 2.7 Remover botón de prueba `"Simular MCP"` para la versión limpia de producción.

## Fase 3: Identidad Visual y Protección de Datos
- [x] 3.1 Actualizar icono del launcher (`ic_launcher_foreground.xml`, `ic_launcher_background.xml` y mipmaps) con el vector y recursos oficiales de Bubble (eliminando el robot genérico).
- [x] 3.2 Crear `data_extraction_rules.xml` y `backup_rules.xml` en `BubbleAgent/app/src/main/res/xml/` y vincular en `AndroidManifest.xml` (`android:dataExtractionRules` y `android:fullBackupContent`) protegiendo `bubble.db` de respaldos en nube no autorizados.
- [x] 3.3 Validar coherencia de AppFunctions (`readUserProfile` y `saveUserProfile`) en `bubble_app_function_service.xml`.

## Fase 4: Verificación Pre-Vuelo y Políticas de Google Play
- [x] 4.1 Ejecutar suite completa de tests unitarios (`./gradlew testDebugUnitTest` - 100% pasando).
- [x] 4.2 Inspección pre-vuelo del archivo `androidApp-release.aab` generado (versionCode 20, versionName "1.2.1", targetSdk 36, firmado con keystore oficial y SHA-256 verificado `77:CF:...:C0:5C` mediante `verify-release-aab.sh`).
- [x] 4.3 Checklist de políticas y privacidad:
  - Documento de privacidad creado en `docs/index.html` y `docs/privacy-policy.html` enfocado en Local-First AI, cero telemetría externa, exclusión de copias en nube de `bubble.db` y no recolección de ID de publicidad.
  - Respuestas oficiales para Google Play Console preparadas:
    - Declaración de ID de publicidad: "No".
    - Apps de salud: "No, mi app no es una app de salud ni ofrece funciones relacionadas con la salud".
    - Seguridad de los datos (Data Safety): No recopila ni comparte datos con terceros (100% on-device).
    - Permiso `PACKAGE_USAGE_STATS`: Uso exclusivo local mediante `UsageStatsManager` para cálculo de tiempo en pantalla vs. potencial offline.
