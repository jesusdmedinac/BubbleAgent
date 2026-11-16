# Bubble Migration Plan to Kotlin Multiplatform

Goal: Migrate from a single Android module (`:app`) to a Kotlin Multiplatform (KMP) project sharing code across Android and iOS, **without breaking the Android version submitted to production** (versionCode 20, `com.jesusdmedinac.bubble`).

Baseline state (2026-11-03, `kmp describe`): Gradle, Kotlin 2.3.20, AGP 9.1.0, single `:app` module, Android target only, ~7,850 lines across 60 files.

---

## 0. Open Decisions & Architecture Records

| # | Decision | Default / Resolution |
|---|---|---|
| D1 | ~~iOS UI: Shared Compose Multiplatform vs. Native SwiftUI~~ | **Decided (2026-11-03):** Screen content in shared Compose Multiplatform; native iOS Human Interface Guidelines shell with Liquid Glass in SwiftUI. See Section 2.1. |
| D2 | AI Engine on iOS (Gemini Nano is not available there) | Shared interface `BubbleAiEngine`. iOS starts with `MockAiEngine` and will evaluate Apple Foundation Models. |
| D3 | Screen Time on iOS (FamilyControls/DeviceActivity does not permit free querying) | Shared interface `ScreenTimeSource`. On iOS, starts with demo mode or manual entries. |
| D4 | Whether Android 1.2.0 release ships before or after migrating | **Before**: ship initial release from `:app` and migrate cleanly on a feature branch. |
| D5 | Android version support (`minSdk 26` vs. `minSdk 29+`) | **Decided (2026-11-15):** Two-phase strategy. For v1.2.1 (20), maintain `minSdk = 26` with defensive fallback code to avoid invalidating the bundle under review in Google Play. For v2.0 (Live Bubble Mascot in Canvas), raise `minSdk` to 29 (or 31) to eliminate legacy technical debt. |

---

## 1. Inventory: What is Shared vs. Platform-Specific

### Migrates to `commonMain` with Minimal or No Changes
- `data/model/*` (7 files): Replaced `java.util.UUID` with `kotlin.uuid.Uuid` and `System.currentTimeMillis()` with `kotlin.time.Clock.System`.
- `data/repository/BubbleRepository.kt` (512 lines, zero Android imports).
- `data/DataRepository.kt`, `ai/vault/VaultMemoryDetector.kt` (UUID only), `ai/koog/GeminiNanoKoogClient.kt` (no direct Android dependency).
- `ui/theme/*` and other UI files in `ui/main` and `ui/profile`, including `ui/main/sdui/BubbleSduiTemplates.kt`.
- `BubbleDatabase.sq` (SQLDelight 2.4.1 is multiplatform native).

### Dependencies: Multiplatform Verified
- **Koog** `agents-core`: Published variants for `iosarm64` and `iossimulatorarm64`.
- **json-to-compose** (`com.jesusdmedinac`): Multiplatform artifacts for `iosarm64`, `iossimulatorarm64`, `iosx64`, `desktop`, and `wasm-js`.
- **SQLDelight**: `native-driver` for iOS and `android-driver` for Android.
- **Coroutines, Serialization**: Fully multiplatform.

### JetBrains Multiplatform Equivalents
- `androidx.compose.*` (BOM) → `org.jetbrains.compose` + `compose.material3`, `compose.ui`, `compose.components.resources`.
- `material-icons-extended` → Compose Multiplatform equivalent.
- `lifecycle-viewmodel-compose` / `lifecycle-runtime-compose` → `org.jetbrains.androidx.lifecycle:*`.
- `navigation3-ui` → `org.jetbrains.androidx.navigation3:navigation3-ui`.
- `@Preview` → Multiplatform previews or previews scoped to `androidMain`.

### Stays in `androidMain` Behind Domain Contracts

| File | Why Android-Only | Shared Contract |
|---|---|---|
| `ai/BubbleAgentManager.kt` | ML Kit GenAI (Gemini Nano/AICore), `Context`, `Log` | `BubbleAiEngine` |
| `ai/BubbleAiEngine.kt` (impls), `ai/koog/BubbleKoogAgent.kt` | Accepts `Context` | Shared interface in common; concrete impls in androidMain |
| `appfunctions/BubbleAppFunctionService.kt` | `androidx.appfunctions` + KSP | Android only, delegates to shared `BubbleRepository` |
| `data/tracker/ScreenTimeTracker.kt` | `UsageStatsManager`, `AppOpsManager`, `Settings` | `ScreenTimeSource` interface |
| `ui/main/FloatingPromptBar.kt` | `RecognizerIntent` (dictation), `Toast` | `SpeechInput` abstraction + `onMicClick` lambda |
| `ui/main/BubbleAssistantActionBar.kt` | `ClipData`, share `Intent` | `LocalClipboard` + `ShareSheet` interface |
| `ui/main/MainScreen.kt`, `ui/profile/ProfileVaultScreen.kt` | `Context`, `LifecycleEventObserver` | `LifecycleEventEffect` from multiplatform lifecycle |
| `ui/main/MainScreenViewModel.kt`, `ui/profile/ProfileVaultViewModel.kt` | ViewModels | Receive domain interfaces, never Android `Context` |
| `BubbleApplication.kt` (`AppContainer`) | `Application`, `AndroidSqliteDriver` | Platform driver factories |
| `MainActivity.kt` | Android entry point | Kept in `:androidApp` |

---

## 2. Target Project Structure

```
BubbleAgent/
├── shared/          # KMP: com.android.kotlin.multiplatform.library + kotlin.multiplatform + compose
│   └── src/
│       ├── commonMain/   # Models, repository, SQLDelight, ViewModels, Compose UI, Koog
│       ├── androidMain/  # Gemini Nano, ScreenTimeTracker, Android driver, platform actions
│       └── iosMain/      # Native SQLite driver, iOS contract impls, MainViewController
├── androidApp/      # com.android.application: MainActivity, AppFunctions, manifest, signing, R8
└── iosApp/          # Xcode project consuming :shared framework with SwiftUI Liquid Glass shell
```

### 2.1 Platform Style Strategy: Material 3 Expressive on Android, Liquid Glass on iOS
- **SwiftUI owns navigation and system controls on iOS**: `NavigationStack`, system navigation bar, toolbar with `.glassEffect`.
- **Compose renders screen content**: Each screen (`Main`, `ProfileVault`) is displayed inside its own `UIViewController` created from Kotlin (`MainViewController`).
- **Navigation dispatched via Swift**: Compose notifies when navigation is requested, and the Swift coordinator pushes routes onto the native stack.

---

## 3. Execution Phases

### Phase 0: Clean Baseline — Completed 2026-11-03
- [x] 0.1 Commit pending changes on `main` (`2af23b2`).
- [x] 0.2 Create `feature/kmp-migration` branch.

### Phase 1: Code Preparation within `:app` — Completed 2026-11-03
- [x] 1.1 Multiplatform time and UUID helpers (`core/Time.kt`). Commit `3c69c24`.
- [x] 1.2 SQLDelight migration `1.sqm` with snapshot `1.db` and verification tests. Commit `4796a2b`.
- [x] 1.3 Abstract contracts: `ScreenTimeSource`, `SpeechInput`, `ShareSheet`. Commits `f2557a4` and `412df16`.
- [x] 1.4 Platform-neutral `AppGraph` dependency injection. Commit `f2557a4`.

### Phase 2: Modular Split into `:shared` and `:androidApp` — Completed 2026-11-03
- [x] 2.1 Version catalog updates for multiplatform plugins and libraries.
- [x] 2.2 Configure `:shared` with `com.android.kotlin.multiplatform.library` and iOS targets (`iosArm64`, `iosSimulatorArm64`). Commit `96731ff`.
- [x] 2.3 Retain identical `applicationId`, versioning, and keystore in `:androidApp`.

### Phase 3: UI in Compose Multiplatform — Completed 2026-11-03
- [x] 3.1 Migrate UI composables and resources to `commonMain`. Commit `58f8e40`.
- [x] 3.2 ViewModels and tests moved to `commonMain` and `commonTest`.
- [x] 3.3 Navigation 3 and NavigationEvent multiplatform integration.

### Phase 4: iOS Target & SwiftUI Liquid Glass Shell — Completed 2026-11-03
- [x] 4.0 Native SwiftUI `NavigationStack` with Compose content and floating glass prompt bar. Commit `9b15e5c`.
- [x] 4.1 Native SQLite driver and `BubbleIosBridge` for reactive state observation.
- [x] 4.2 Verified on physical iPhone and iOS simulator.

### Phase 5: Publishing & Production Verification — Completed 2026-11-15
- [x] 5.1 Automated AAB verification script (`verify-release-aab.sh`) validating SHA-256 (`77:CF:...:C0:5C`), Target SDK 36, and versionCode.
- [x] 5.2 Public Privacy Policy generated in `docs/index.html` and `docs/privacy-policy.html`.
- [x] 5.3 Android release built from `:androidApp` with `versionCode = 20` and `versionName = "1.2.1"`, signed and promoted to Closed and Open Testing in Google Play Console.
