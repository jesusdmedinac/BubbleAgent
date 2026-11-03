# Plan de migración de Bubble a Kotlin Multiplatform

Objetivo: pasar de un solo módulo Android (`:app`) a un proyecto KMP con código compartido para Android e iOS, **sin romper la versión de Android que está por publicarse** (versionCode 19, `com.jesusdmedinac.bubble`).

Estado de partida (2026-11-03, `kmp describe`): Gradle, Kotlin 2.3.20, AGP 9.1.0, un módulo `:app`, target solo Android, unas 7,850 líneas en 60 archivos.

---

## 0. Decisiones abiertas

| # | Decisión | Opción por defecto de este plan |
|---|---|---|
| D1 | ~~UI en iOS: Compose Multiplatform compartido o SwiftUI nativo~~ | **Decidido (2026-11-03):** contenido en Compose Multiplatform compartido; en iOS, estilo nativo de las Human Interface Guidelines con Liquid Glass. Ver sección 2.1 |
| D2 | Motor de IA en iOS (Gemini Nano no existe ahí) | Interfaz común `BubbleAiEngine`. En iOS empieza con `MockAiEngine` y después Apple Foundation Models |
| D3 | Tiempo en pantalla en iOS (FamilyControls/DeviceActivity no deja leer métricas libremente) | Interfaz común `ScreenTimeSource`. En iOS, por ahora, el modo demo o un registro manual |
| D4 | Si el release 1.2.0 de Android sale antes o después de migrar | **Antes**: publicar 1.2.0 desde `:app` y migrar en una rama |

---

## 1. Inventario: qué se comparte y qué no

### Va a `commonMain` sin cambios o con cambios mínimos
- `data/model/*` (7 archivos). Solo hay que reemplazar `java.util.UUID` por `kotlin.uuid.Uuid` y `System.currentTimeMillis()` por `kotlin.time.Clock.System`.
- `data/repository/BubbleRepository.kt` (512 líneas, sin imports de Android).
- `data/DataRepository.kt`, `ai/vault/VaultMemoryDetector.kt` (solo usa UUID), `ai/koog/GeminiNanoKoogClient.kt` (no usa Android directamente).
- `ui/theme/*` y los otros 30 archivos de `ui/main` y `ui/profile`, incluido `ui/main/sdui/BubbleSduiTemplates.kt`.
- `BubbleDatabase.sq` (SQLDelight 2.4.1 ya es multiplataforma).

### Dependencias: ya son multiplataforma (verificado en la caché de Gradle)
- **Koog** `agents-core`: hay variantes `iosarm64` e `iossimulatorarm64`.
- **json-to-compose** (`com.jesusdmedinac`): hay variantes `iosarm64`, `iossimulatorarm64`, `iosx64`, `desktop` y `wasm-js`.
- **SQLDelight**: `native-driver` para iOS y `android-driver` para Android.
- **Coroutines, serialization**: multiplataforma.

### Hay que cambiar a la versión de JetBrains (multiplataforma)
- `androidx.compose.*` (BOM) → plugin `org.jetbrains.compose` + `compose.material3`, `compose.ui`, `compose.components.resources`.
- `material-icons-extended` → el equivalente de Compose Multiplatform. Revisar su estado de mantenimiento al momento de migrar; si está deprecado, usar íconos en `composeResources`.
- `lifecycle-viewmodel-compose` / `lifecycle-runtime-compose` → `org.jetbrains.androidx.lifecycle:*`.
- `navigation3-runtime` / `navigation3-ui` → los artefactos de Navigation 3 para Compose Multiplatform (`org.jetbrains.androidx.navigation3:*`). **Verificar** su versión y estabilidad antes de comprometerse; si no están listos, dejar `Navigation.kt` en `androidMain` y usar un enrutador propio en iOS.
- `@Preview` de AndroidX → previews de Compose Multiplatform o dejar las previews en `androidMain`.

### Se queda en `androidMain` detrás de una interfaz común

| Archivo | Por qué es Android-only | Contrato común |
|---|---|---|
| `ai/BubbleAgentManager.kt` | ML Kit GenAI (Gemini Nano/AICore), `Context`, `Log` | `BubbleAiEngine` (ya existe) |
| `ai/BubbleAiEngine.kt` (impls), `ai/koog/BubbleKoogAgent.kt` | reciben `Context` | la interfaz pasa a common y las implementaciones con `Context` a androidMain |
| `appfunctions/BubbleAppFunctionService.kt` | `androidx.appfunctions` + KSP | solo Android, consume `BubbleRepository` común |
| `data/tracker/ScreenTimeTracker.kt` | `UsageStatsManager`, `AppOpsManager`, `Settings`, `Calendar` | nueva interfaz `ScreenTimeSource` |
| `ui/main/FloatingPromptBar.kt` | `RecognizerIntent` (dictado), `Toast`, activity result | `SpeechInput` como `expect`/interfaz y un lambda `onMicClick` |
| `ui/main/BubbleAssistantActionBar.kt` | `ClipData`, `Intent` para compartir | `LocalClipboard` de Compose + interfaz `ShareSheet` |
| `ui/main/MainScreen.kt`, `ui/profile/ProfileVaultScreen.kt` | `Context`, `LifecycleEventObserver` | `LifecycleEventEffect` de lifecycle-compose multiplataforma |
| `ui/main/MainScreenViewModel.kt`, `ui/profile/ProfileVaultViewModel.kt` | `Context` en el constructor | recibir interfaces comunes, no `Context` |
| `BubbleApplication.kt` (`AppContainer`) | `Application`, `AndroidSqliteDriver` | `expect fun createSqlDriver()` o `DriverFactory` por plataforma |
| `MainActivity.kt` | punto de entrada Android | queda en `:androidApp` |

### Riesgo encontrado: migraciones de base de datos escritas a mano
`AppContainer` aplica `CREATE TABLE ConversationEntity` y dos `ALTER TABLE` en `onOpen` de `AndroidSqliteDriver.Callback`. En iOS ese callback no existe. **Antes de compartir la base**, hay que convertirlo en migraciones `.sqm` de SQLDelight con `verifyMigrations`, para que el esquema sea igual en ambas plataformas y los usuarios actuales de Android no pierdan datos.

---

## 2. Estructura destino

```
BubbleAgent/
├── shared/          # KMP: com.android.kotlin.multiplatform.library + kotlin.multiplatform + compose
│   └── src/
│       ├── commonMain/   # modelos, repositorio, SQLDelight, ViewModels, UI Compose, Koog
│       ├── androidMain/  # Gemini Nano, ScreenTimeTracker, driver Android, dictado, compartir
│       └── iosMain/      # driver nativo, implementaciones iOS de las interfaces, MainViewController
├── androidApp/      # com.android.application: MainActivity, AppFunctions, manifest, firma, R8
└── iosApp/          # proyecto Xcode que consume el framework de :shared
```

Con AGP 9, el plugin `org.jetbrains.kotlin.multiplatform` ya no se combina con `com.android.application` en el mismo módulo. Por eso el código compartido vive en `:shared` (plugin `com.android.kotlin.multiplatform.library`) y la app Android en su propio módulo. El skill `agp-9-upgrade` no cubre proyectos KMP; para esto conviene consultar la documentación oficial de AGP 9 y de KMP.

`applicationId`, `namespace` de la app, keystore, `proguard-rules.pro`, `data_extraction_rules.xml`, `backup_rules.xml` y `bubble_app_function_service.xml` se mueven tal cual a `:androidApp`, para que Play vea la misma app.

### 2.1 Estilo por plataforma: Material 3 Expressive en Android, Liquid Glass en iOS

Liquid Glass real solo existe en los componentes nativos de iOS 26 (`glassEffect`, `GlassEffectContainer` en SwiftUI, `UIGlassEffect` en UIKit). Compose no lo puede dibujar: a lo más imitaría un desenfoque, y se notaría. compose-calculator ya lo resolvió en SwiftUI (`iosApp/CalculatorGlassView.swift`: `GlassEffectContainer` + `.glassEffect(.regular.tint(...).interactive(), in: .circle)`, deployment target 26.6), así que ese archivo sirve de referencia.

Por eso Bubble se divide en dos capas:

| Capa | Qué incluye | Android | iOS |
|---|---|---|---|
| **Contenido** (compartido, Compose) | mensajes del chat, `DigitalHealthCard`, actividades recomendadas, donut de tiempo en pantalla, tarjetas del Vault, plantillas SDUI | Compose + tema M3 Expressive | el mismo Compose con un tema iOS: fuente del sistema (SF), colores y radios de las HIG, sin ripple, scroll con rebote |
| **Controles** (por plataforma) | barra superior, `FloatingPromptBar` + botón de enviar/audio, menú lateral, bottom sheets, diálogos, botones flotantes | composables actuales de Material 3 | SwiftUI nativo con Liquid Glass: `NavigationStack`/toolbar, barra de prompt de vidrio, `.sheet`, `.alert`, menú como sidebar o sheet |

Cómo se arma en iOS:
- La raíz es SwiftUI. El contenido Compose entra como `ComposeUIViewController` dentro de un `UIViewControllerRepresentable`, **debajo**, y los controles de vidrio van **encima** en un `ZStack`. Así el vidrio refracta el contenido real que pasa por detrás, como en una app nativa.
- El estado sale de los ViewModels compartidos. SwiftUI los observa con SKIE (`StateFlow` → `AsyncSequence`), siguiendo el skill `kotlin-interop-swift-coroutines`.
- Las acciones de los controles (enviar prompt, abrir el Vault, adjuntar contexto) llaman a métodos del ViewModel compartido. No se duplica la lógica en Swift.
- El contenido Compose deja espacio arriba y abajo para los controles (insets compartidos), para que nada quede tapado.
- Deployment target **iOS 26**, igual que compose-calculator. Si se quisiera soportar iOS 17–25, el respaldo sería `.ultraThinMaterial`, pero no es Liquid Glass.

En Android no cambia nada: los controles siguen siendo los composables actuales. En código común, cada control se declara como un slot (`topBar`, `promptBar`, `sheets`) que `:androidApp` llena con Material 3 y que en iOS se deja vacío porque lo pone SwiftUI.

Riesgo a validar al inicio de la fase 4: que el vidrio de SwiftUI muestre bien el contenido Compose que pasa por detrás mientras se hace scroll, y que los gestos (scroll, tap en mensajes) lleguen a Compose a través de la capa de controles. Si falla, el plan B es pasar a SwiftUI también el contenido de la pantalla de chat.

---

## 3. Fases

Cada fase termina compilando y con `./gradlew testDebugUnitTest` en verde. La app Android tiene que verse y funcionar igual al final de cada fase.

### Fase 0: Punto de partida limpio
- [x] 0.1 Commitear los 10 archivos modificados que hay en `BubbleAgent` (commit `2af23b2` en `main`; 17 tests en verde y `assembleDebug` correcto).
- [ ] 0.2 Decidir D4. Si 1.2.0 sale primero, cerrar las tareas 4.2 y 4.3 de `PROGRESS.md` y etiquetar `v1.2.0`.
- [x] 0.3 Crear la rama `feature/kmp-migration`.
- [ ] 0.4 Ambiente: `export JAVA_HOME=$(/usr/libexec/java_home)` en `~/.zshrc` y el plugin de Kotlin Multiplatform en Android Studio (avisos de `kmp doctor`).

### Fase 1: Preparar el código dentro de `:app` (sin KMP todavía)
Cambios pequeños que dejan el código listo para moverse, y que se pueden probar con la app actual.
- [ ] 1.1 `UUID` → `kotlin.uuid.Uuid` y `System.currentTimeMillis()` → `Clock.System.now().toEpochMilliseconds()` en modelos, VaultMemoryDetector y MainScreenViewModel.
- [ ] 1.2 Convertir las migraciones manuales de `AppContainer.onOpen` en archivos `.sqm` (versiones 1→2→3) y activar `verifyMigrations`. Probar la actualización desde una instalación de la 1.2.0 con datos.
- [ ] 1.3 Crear las interfaces `ScreenTimeSource`, `SpeechInput` y `ShareSheet`, y hacer que `ScreenTimeTracker`, el dictado y compartir las implementen.
- [ ] 1.4 Quitar `Context` de los constructores de `MainScreenViewModel` y `ProfileVaultViewModel`: que reciban `BubbleRepository`, `BubbleAiEngine` y `ScreenTimeSource`.
- [ ] 1.5 Sustituir `LifecycleEventObserver` manual por `LifecycleEventEffect` en `MainScreen` y `ProfileVaultScreen`.
- [ ] 1.6 Reemplazar `android.util.Log` en código que se compartirá por un logger común mínimo (o Kermit, si se decide agregar la dependencia).

### Fase 2: Módulo `:shared` y separación de `:androidApp`
- [ ] 2.1 Agregar al catálogo: `kotlin-multiplatform`, `org.jetbrains.compose`, `com.android.kotlin.multiplatform.library`, lifecycle y navigation3 de JetBrains, `sqldelight-native-driver`. Verificar cada versión antes de fijarla.
- [ ] 2.2 Crear `:shared` con targets `androidLibrary`, `iosArm64` e `iosSimulatorArm64`.
- [ ] 2.3 Renombrar `:app` a `:androidApp` y conservar `applicationId`, versionCode, firma y R8.
- [ ] 2.4 Mover `BubbleDatabase.sq` y las `.sqm` a `shared/src/commonMain/sqldelight` y crear `DriverFactory` (`expect`/`actual`).
- [ ] 2.5 Mover modelos, repositorio, `VaultMemoryDetector`, `BubbleAiEngine` (interfaz + `MockAiEngine`) y `GeminiNanoKoogClient` a `commonMain`.
- [ ] 2.6 Mover `BubbleAgentManager`, `BubbleKoogAgent`, `KoogGeminiNanoAiEngine` y `ScreenTimeTracker` a `shared/androidMain` o a `:androidApp`.
- [ ] 2.7 `BubbleAppFunctionService` y su KSP se quedan en `:androidApp`.
- [ ] 2.8 Mover los tests unitarios de modelos, repositorio y ViewModels a `commonTest`.
- [ ] 2.9 Verificar: `./gradlew :androidApp:assembleDebug`, los tests y una prueba manual en el dispositivo (chat con Gemini Nano, Vault, tiempo en pantalla, AppFunctions por `adb`).

### Fase 3: UI en Compose Multiplatform
- [ ] 3.0 Separar contenido de controles (sección 2.1): `MainScreenContent` y `ProfileVaultContent` reciben slots `topBar`, `promptBar` y `sheets`, y un parámetro de insets. Android los llena con los composables actuales.
- [ ] 3.1 Mover `ui/theme` y los composables sin dependencias Android a `commonMain`. Los textos y fuentes van a `composeResources`. Agregar un `BubbleTheme` con dos variantes: M3 Expressive (Android) e iOS (fuente del sistema, colores y radios de las HIG).
- [ ] 3.2 Mover `MainScreenViewModel` y `ProfileVaultViewModel` a `commonMain` (lifecycle multiplataforma).
- [ ] 3.3 `FloatingPromptBar` y `BubbleAssistantActionBar` reciben `SpeechInput` y `ShareSheet` inyectados; el portapapeles usa `LocalClipboard`.
- [ ] 3.4 Navegación: Navigation 3 multiplataforma si está estable, o mantener `Navigation.kt` en Android y un enrutador simple en common.
- [ ] 3.5 Revisar que las previews sigan funcionando en Android Studio.
- [ ] 3.6 Verificar que la app Android se ve y funciona igual que la 1.2.0 (capturas comparativas).

### Fase 4: Target iOS
- [ ] 4.0 Prueba técnica corta: una pantalla de chat de muestra con contenido Compose debajo y una barra de prompt SwiftUI con `.glassEffect` encima. Validar refracción durante el scroll, gestos y teclado. Decidir aquí si se sigue el plan A o el B (sección 2.1).
- [ ] 4.1 Crear `iosApp` (Xcode, deployment target iOS 26) con raíz SwiftUI que incruste el contenido de `:shared` mediante `ComposeUIViewController`.
- [ ] 4.1.1 Agregar SKIE a `:shared` para que SwiftUI observe los `StateFlow` de los ViewModels.
- [ ] 4.1.2 Controles Liquid Glass en SwiftUI, tomando `CalculatorGlassView.swift` como referencia: barra superior, barra de prompt con botón de enviar/audio, menú lateral, sheets (adjuntar contexto, info del modelo local) y diálogo de seguridad del Vault.
- [ ] 4.2 `actual` iOS: `NativeSqliteDriver`, `ScreenTimeSource` (modo demo, según D3), `SpeechInput` (Speech framework), `ShareSheet` (`UIActivityViewController`).
- [ ] 4.3 Motor de IA en iOS según D2: arrancar con `MockAiEngine` y después evaluar Apple Foundation Models detrás de `BubbleAiEngine`.
- [ ] 4.4 Revisar que los textos de UI no muestren nombres técnicos (por ejemplo "Gemini Nano On-Device" o "Android MCP Bridge" en iOS).
- [ ] 4.5 Correr la app en el simulador y en un iPhone.

### Fase 5: Publicación y herramientas
- [ ] 5.1 Traer de compose-calculator el script de revisión del AAB, fastlane (Play y App Store) y el workflow de GitHub Actions, adaptados a `:androidApp`.
- [ ] 5.2 Política de privacidad que cubra las dos plataformas.
- [ ] 5.3 Release Android desde `:androidApp` con versionCode mayor a 19 y la misma firma.

---

## 4. Riesgos

| Riesgo | Mitigación |
|---|---|
| Perder datos de usuarios de Android al cambiar las migraciones | Fase 1.2 con prueba de actualización real antes de mover nada |
| Navigation 3 o íconos sin versión multiplataforma estable | Mantenerlos en `androidMain` por ahora; no bloquean la fase 2 |
| AppFunctions y KSP dentro de un proyecto KMP | Dejarlos en `:androidApp`, que sigue siendo un módulo Android puro |
| iOS sin IA local equivalente ni métricas de pantalla | Interfaces comunes desde la fase 1; las decisiones D2 y D3 no bloquean Android |
| Cambio de firma o de `applicationId` al reestructurar | Verificar el AAB contra la huella SHA-256 del keystore (script de compose-calculator) |

## Skills de apoyo
`kmp-cli` (`kmp doctor`, `kmp describe`, `kmp analyze`), `kotlin-data-sqldelight-database` (fase 1.2 y 2.4), `kmp-shared-core-bridge` (fase 2), `kotlin-ui-compose-adaptive` (fase 3), `kotlin-interop-swift-coroutines` (fase 4: SKIE para los controles SwiftUI con Liquid Glass).
