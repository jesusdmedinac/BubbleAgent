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
- `navigation3-ui` → `org.jetbrains.androidx.navigation3:navigation3-ui`. Compose Multiplatform lo soporta en todas las plataformas desde la 1.10 (la documentación oficial menciona la 1.1.1). Solo `navigation3-ui` tiene versión de JetBrains; `navigation3-common` llega como dependencia transitiva. En iOS no hay serialización por reflexión, así que `rememberNavBackStack` debe usar la variante con `SavedStateConfiguration` y registrar `Main` y `ProfileVault` en un `SerializersModule`.
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

Con AGP 9, el plugin `org.jetbrains.kotlin.multiplatform` ya no se combina con `com.android.application` en el mismo módulo. Por eso el código compartido vive en `:shared` (plugin `com.android.kotlin.multiplatform.library`) y la app Android en su propio módulo. El skill `agp-9-upgrade` no cubre proyectos KMP; las guías que aplican son las de JetBrains en la base de conocimiento de `android docs` (ver sección 5).

Reglas confirmadas en esa documentación:
- **Los namespaces deben ser distintos**: `:androidApp` conserva `com.jesusdmedinac.bubble` y `:shared` usa `com.jesusdmedinac.bubble.shared`. Si coinciden, hay clases `R` duplicadas. Los paquetes de Kotlin pueden seguir siendo `com.jesusdmedinac.bubble.*`.
- `:shared` no lleva `applicationId`, `versionCode`, `targetSdk`, `buildTypes` ni flavors. El plugin KMP de Android produce una sola variante.
- `:androidApp` no debe aplicar `org.jetbrains.kotlin.android`: AGP 9 ya trae Kotlin integrado.
- Versiones mínimas: Gradle 9.1, JDK 17, Build Tools 36, KSP 2.3.1+ (Bubble usa 2.3.12) y Compose Multiplatform 1.9.3+ (recomendada 1.10+).
- Android Studio Otter 3 o posterior. IntelliJ IDEA no soporta AGP 9 con Android.
- La tabla de compatibilidad marca API 36.1 como máximo para AGP 9.0. Bubble usa `compileSdk = 37` con AGP 9.1, y hoy compila como app; hay que confirmar que el plugin KMP de Android también lo acepte.
- Dos archivos usan `com.jesusdmedinac.bubble.R`. Al mover sus composables a `commonMain`, esos recursos pasan a `composeResources` (`Res.drawable` / `Res.string`).

`applicationId`, `namespace` de la app, keystore, `proguard-rules.pro`, `data_extraction_rules.xml`, `backup_rules.xml` y `bubble_app_function_service.xml` se mueven tal cual a `:androidApp`, para que Play vea la misma app.

### 2.1 Estilo por plataforma: Material 3 Expressive en Android, Liquid Glass en iOS

Liquid Glass real solo lo dibuja el sistema en componentes nativos de iOS 26. Compose no lo puede reproducir. La guía oficial de JetBrains ("Liquid Glass in a Compose Multiplatform app", `kb://JetBrains/kotlin-multiplatform-dev-docs/topics/ios-liquid-glass`, con la app de KotlinConf como ejemplo, rama `lg-nav`) resuelve exactamente este caso, y el plan la sigue:

- **SwiftUI es dueño de la navegación y de los controles del sistema**: `NavigationStack`, barra de navegación, toolbar y, si algún día hay pestañas, `TabView`. Al usarlos, iOS aplica Liquid Glass solo, sin código especial.
- **Compose dibuja el contenido de cada pantalla**. Cada destino (`Main`, `ProfileVault`) se muestra en su propio `UIViewController` creado desde Kotlin.
- **La navegación pasa por Swift**: cuando Compose quiere navegar (por ejemplo, abrir el Vault), avisa por un callback `onNavigate`. El coordinador de Swift empuja la ruta en su `NavigationStack`. En iOS, Compose deja de manejar el back stack.
- **Compose oculta su propia barra superior en iOS**, para no duplicar títulos ni botones de regreso. En Android no cambia nada.
- **Las rutas llevan su título** (`title` en cada `NavKey`), para que SwiftUI lo muestre sin preguntarle a Kotlin.

| Capa | Android | iOS |
|---|---|---|
| Navegación y barras | Navigation 3 + `BubbleTopAppBar` (M3) | `NavigationStack` + toolbar de SwiftUI (Liquid Glass del sistema) |
| Contenido de cada pantalla | Compose, tema M3 Expressive | el mismo Compose, con tema iOS (fuente del sistema, colores y radios de las HIG, sin ripple) |
| Barra de prompt flotante | `FloatingPromptBar` (M3) | SwiftUI con `.glassEffect`, como `safeAreaInset` inferior; toma como referencia `CalculatorGlassView.swift` de compose-calculator |
| Menú lateral, sheets, diálogos | composables actuales | `.sheet`, `.alert` y un menú nativo |

El estado lo siguen dando los ViewModels compartidos. Lo que SwiftUI necesita observar directamente (texto del prompt, si está generando, título de la conversación) se expone con SKIE (skill `kotlin-interop-swift-coroutines`).

Deployment target **iOS 26**, con Xcode 26 o posterior.

Alternativas que menciona la guía, por si el enfoque principal no funciona: navegación UIKit coordinada desde Kotlin con `expect`/`actual`; controles nativos incrustados en Compose (con limitaciones de interop); componentes adaptativos de terceros como Calf; o imitar el vidrio en Compose (AndroidLiquidGlass, Liquid), que se parece pero no es idéntico.

Lo único fuera de la guía es la barra de prompt de vidrio encima del contenido Compose. Esa es la prueba técnica de la fase 4.0.

---

## 3. Fases

Cada fase termina compilando y con `./gradlew testDebugUnitTest` en verde. La app Android tiene que verse y funcionar igual al final de cada fase.

### Fase 0: Punto de partida limpio
- [x] 0.1 Commitear los 10 archivos modificados que hay en `BubbleAgent` (commit `2af23b2` en `main`; 17 tests en verde y `assembleDebug` correcto).
- [ ] 0.2 Decidir D4. Si 1.2.0 sale primero, cerrar las tareas 4.2 y 4.3 de `PROGRESS.md` y etiquetar `v1.2.0`.
- [x] 0.3 Crear la rama `feature/kmp-migration`.
- [ ] 0.4 Ambiente: `export JAVA_HOME=$(/usr/libexec/java_home)` en `~/.zshrc` y el plugin de Kotlin Multiplatform en Android Studio (avisos de `kmp doctor`).

### Fase 1: Preparar el código dentro de `:app` (sin KMP todavía) — terminada el 2026-11-03
Cambios pequeños que dejan el código listo para moverse, y que se pueden probar con la app actual.
- [x] 1.1 `UUID` y `System.currentTimeMillis()` → `randomId()` y `currentTimeMillis()` en `core/Time.kt` (Kotlin `Uuid` y `Clock`). Commit `3c69c24`.
- [x] 1.2 Migración `migrations/1.sqm` con la instantánea `databases/1.db` y `verifyMigrations`. Las bases que ya tenían el parche manual (versión 1 con las columnas nuevas) se marcan como versión 2 al abrir. Prueba JVM `BubbleDatabaseMigrationTest`. Probado en el Galaxy S24 Ultra: una base parchada con 36 mensajes y 3 memorias quedó en versión 2 sin perder nada. Commit `4796a2b`.
- [x] 1.3 Interfaces `ScreenTimeSource` (con `demoScreenTimeSnapshot`), `SpeechInput` y `ShareSheet`, más `plainTextClipEntry`. Las implementaciones Android están en `ScreenTimeTracker` y `ui/platform/PlatformActions.kt`. Commits `f2557a4` y `412df16`.
- [x] 1.4 `AppGraph` común; `AppContainer` lo implementa y `Navigation.kt` crea los ViewModels desde él. Ningún ViewModel usa `Context` ni `BubbleApplication.instance`. Commit `f2557a4`.
- [x] 1.5 `LifecycleEventEffect` en `MainScreen` y `ProfileVaultScreen`. Commit `f2557a4`.
- [x] 1.6 No hizo falta: `android.util.Log` solo aparece en `BubbleAgentManager`, que se queda en Android.
- Extra: `isGenerating` se marca antes de insertar los mensajes nuevos (causaba una prueba intermitente) y las pruebas de ViewModel usan un dispatcher de prueba. 19 pruebas, 5 corridas seguidas en verde. Commit `412df16`.

Quedan con APIs de Android dentro de la UI, para la fase 3: `BackHandler` de `androidx.activity` en las dos pantallas, `com.jesusdmedinac.bubble.R` en dos archivos y los `@Preview` de AndroidX.

### Fase 2: Módulo `:shared` y separación de `:androidApp` — terminada el 2026-11-03
- [x] 2.1 Catálogo: plugins `kotlin-multiplatform` y `android-kotlin-multiplatform-library`, librerías `sqldelight-native-driver` y `kotlinx-coroutines-core`. Lifecycle y Navigation 3 de JetBrains se agregan en la fase 3, cuando se mueva la UI.
- [x] 2.2 `:shared` con `com.android.kotlin.multiplatform.library` (namespace `com.jesusdmedinac.bubble.shared`, compileSdk 37 sin problemas) y targets `iosArm64` e `iosSimulatorArm64`. El framework de iOS se declara en la fase 4.
- [x] 2.3 `:app` → `:androidApp` con el mismo `applicationId`, namespace, versión, firma y R8. El AAB de release sigue siendo `com.jesusdmedinac.bubble` 1.2.0 (19), firmado con el keystore de siempre.
- [x] 2.4 `BubbleDatabase.sq`, `migrations/1.sqm` y `databases/1.db` en `shared/src/commonMain/sqldelight`. No hizo falta `expect`/`actual` todavía: Android crea su driver en `AppContainer` (que también marca las bases parchadas) y el driver nativo se agrega en la fase 4.
- [x] 2.5 En `commonMain`: `AppGraph`, `core/Time.kt`, `data/model/*`, `BubbleRepository` (con `Dispatchers.IO` multiplataforma), `ScreenTimeSource`, `BubbleAiEngine` + `MockAiEngine` y `VaultMemoryDetector`. Se borró `DataRepository.kt`, que era código de plantilla sin uso.
- [x] 2.6 Se quedan en `:androidApp`: `BubbleAgentManager`, `BubbleKoogAgent`, `KoogGeminiNanoAiEngine`, `ScreenTimeTracker` y también `GeminiNanoKoogClient`, que depende de `BubbleAgentManager` (el inventario original lo ponía en common por error).
- [x] 2.7 `BubbleAppFunctionService` y su KSP siguen en `:androidApp`; el servicio aparece registrado en el teléfono.
- [x] 2.8 `BubbleDatabaseMigrationTest` pasó a `shared/src/androidHostTest`. Las pruebas de ViewModel se mueven a `commonTest` junto con los ViewModels en la fase 3.
- [x] 2.9 Verificado: `:shared` compila para Android, `iosArm64` e `iosSimulatorArm64`; `verifySqlDelightMigration` pasa; 19 pruebas en verde (17 en `:androidApp`, 2 en `:shared`); `assembleDebug`, `assembleDebugAndroidTest` y `bundleRelease` correctos. `kmp describe` ve los dos módulos y tres targets; `kmp doctor --architecture` solo avisa que falta el punto de entrada de iOS (fase 4). En el Galaxy S24 Ultra la app abre con sus 36 mensajes, 3 memorias y 3 conversaciones.

### Fase 3: UI en Compose Multiplatform
- [ ] 3.0 Separar contenido de controles (sección 2.1): `MainScreenContent` y `ProfileVaultContent` reciben slots `topBar` y `promptBar`, y un parámetro de insets. Android los llena con los composables actuales; en iOS quedan vacíos porque los pone SwiftUI.
- [ ] 3.1 Mover `ui/theme` y los composables sin dependencias Android a `commonMain`. Los textos y fuentes van a `composeResources`. Agregar un `BubbleTheme` con dos variantes: M3 Expressive (Android) e iOS (fuente del sistema, colores y radios de las HIG).
- [ ] 3.2 Mover `MainScreenViewModel` y `ProfileVaultViewModel` a `commonMain` (lifecycle multiplataforma).
- [ ] 3.3 `FloatingPromptBar` y `BubbleAssistantActionBar` reciben `SpeechInput` y `ShareSheet` inyectados; el portapapeles usa `LocalClipboard`.
- [ ] 3.4 Navegación: mover `Navigation.kt` y `NavigationKeys.kt` a `commonMain` con `org.jetbrains.androidx.navigation3:navigation3-ui` y `SavedStateConfiguration` (serializadores polimórficos de `Main` y `ProfileVault`). Agregar `title` a cada ruta y un callback opcional `onNavigate` para iOS (sección 2.1).
- [ ] 3.5 Revisar que las previews sigan funcionando en Android Studio.
- [ ] 3.6 Verificar que la app Android se ve y funciona igual que la 1.2.0 (capturas comparativas).

### Fase 4: Target iOS
- [ ] 4.0 Prueba técnica corta, siguiendo la guía de Liquid Glass (rama `lg-nav` de kotlinconf-app): `NavigationStack` de SwiftUI con la pantalla de chat en Compose, y la barra de prompt con `.glassEffect` como `safeAreaInset` inferior. Validar refracción durante el scroll, gestos y teclado. Si la barra de prompt no funciona bien, elegir una de las alternativas de la sección 2.1.
- [ ] 4.1 Crear `iosApp` (Xcode 26, deployment target iOS 26) con raíz SwiftUI: un coordinador de `NavigationStack` y un `UIViewController` de Compose por destino, creado desde `iosMain`.
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
| Íconos extendidos sin versión multiplataforma mantenida | Revisarlo en la fase 3 con `android docs`; si hace falta, copiar los íconos usados a `composeResources` |
| `compileSdk = 37` fuera del máximo documentado para AGP 9.0 (36.1) | Confirmarlo al crear `:shared` en la fase 2; si falla, bajar `:shared` a 36 sin tocar `:androidApp` |
| AppFunctions y KSP dentro de un proyecto KMP | Dejarlos en `:androidApp`, que sigue siendo un módulo Android puro |
| iOS sin IA local equivalente ni métricas de pantalla | Interfaces comunes desde la fase 1; las decisiones D2 y D3 no bloquean Android |
| Cambio de firma o de `applicationId` al reestructurar | Verificar el AAB contra la huella SHA-256 del keystore (script de compose-calculator) |

## 5. Herramientas: qué usar y qué no (revisado el 2026-11-03)

### `android` CLI (v1.1.16717390): la fuente principal para verificar
| Comando | Uso en este plan |
|---|---|
| `android docs search` / `android docs fetch kb://...` | Documentación oficial actualizada, incluidas las guías de JetBrains para KMP. Se usa para confirmar APIs y versiones antes de fijarlas. Guías clave: `multiplatform-project-agp-9-migration`, `kotlin-tooling-agp9-migration/references/{VERSION-MATRIX,MIGRATION-APP-SPLIT,DSL-REFERENCE}`, `migrate-from-android`, `compose-navigation-3`, `ios-liquid-glass`, `compose-swiftui-integration`, `compose-ios-touch` |
| `android describe` | Lista módulos, variantes y la ruta de cada APK. Al correrlo deja `.gradle/init.gradle.kts`, que ya está ignorado por git |
| `android analyze <aab/apk>` | Lee package, versionName y versionCode del artefacto real. Reemplaza la lectura frágil del manifest que hace el script de compose-calculator (fases 0.2 y 5.1) |
| `android preview list` / `render` | Renderiza los `@Preview` a PNG sin dispositivo. Sirve para comparar la UI antes y después de las fases 2 y 3 (no probado todavía en Bubble) |
| `android run`, `layout`, `screen capture` | Probar la app en dispositivo al cerrar cada fase |
| `android studio version-lookup` | Últimas versiones de artefactos Maven; necesita Android Studio abierto |

El skill `agp-9-upgrade` de Android **no** aplica: dice explícitamente que no sirve para proyectos KMP.

### `kmp` CLI (v0.1.0-SNAPSHOT): útil, con límites
| Comando | Estado |
|---|---|
| `kmp doctor` | Funciona. Avisos actuales: `JAVA_HOME` sin definir, falta el plugin KMP en Android Studio, CocoaPods sin configurar (opcional) |
| `kmp describe` | Funciona. Úsalo al cerrar la fase 2 para confirmar los módulos `shared` y `androidApp` y los targets iOS |
| `kmp doctor --architecture` | Hoy pasa sin hallazgos, pero solo porque no existe `commonMain`. Se vuelve útil desde la fase 2, como revisión de que no entren imports `android.*` a `commonMain` |
| `kmp analyze dependencies` | Funciona; lista bien las 30 librerías y 5 plugins |
| `kmp analyze libraries` | **No sirve para verificar**. El skill documenta `--targets`, pero el binario solo acepta `--json` y `-p/--path`, y su catálogo es corto: no encuentra `navigation3`, `skie` ni `kermit`. Para versiones, usar `android docs` o `android studio version-lookup` |
| `kmp migrate audit/plan/execute` | No aplica: solo audita proyectos React Native y Flutter. Sobre Bubble devuelve un 100/100 vacío |
| `kmp skills add` | Instala en `.agents/skills/` o `~/.kmp/skills/`, no en `~/.claude/skills/`. Para Claude hay que copiar el skill a mano, como se hizo con los seis actuales |
| `kmp mcp` | Servidor MCP por stdio con 9 herramientas; opcional |

El catálogo de `kmp skills list` también incluye `kotlin-ui-sdui-compose` (json-to-compose en KMP). Bubble ya usa json-to-compose, así que vale la pena instalarlo para la fase 3.

## Skills de apoyo
`kmp-cli` (`kmp doctor`, `kmp describe`, `kmp analyze dependencies`), `kotlin-data-sqldelight-database` (fase 1.2 y 2.4), `kmp-shared-core-bridge` (fase 2), `kotlin-ui-compose-adaptive` (fase 3), `kotlin-interop-swift-coroutines` (fase 4: SKIE para los controles SwiftUI con Liquid Glass).
