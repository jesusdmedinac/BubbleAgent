# Bubble - Progress and Release Plan (`com.jesusdmedinac.bubble`)

## Phase 1: Compilation, Versioning, and Cryptographic Signing (Release)
- [x] 1.1 Increment `versionCode = 18` and `versionName = "1.1.0"` in `BubbleAgent/app/build.gradle.kts`. (Updated to `versionCode = 20`, `versionName = "1.2.1"` for Google Play launch).
- [x] 1.2 Target `targetSdk = 36` (and `compileSdk = 37` for AppFunctions compatibility) to meet Google Play 2026 directives.
- [x] 1.3 Configure `signingConfigs.release` in `BubbleAgent/app/build.gradle.kts` using `bubble-keystore`.
- [x] 1.4 Create `BubbleAgent/app/proguard-rules.pro` with R8 keep rules for Compose, SQLDelight, Koog, AppFunctions, and Serialization.
- [x] 1.5 Validate compilation of `app-release.aab` and `app-release.apk` with `versionCode = 18`, and verify signature with `jarsigner -verify` and `apksigner`.
- [x] 1.6 Resolve blocking Google Play Console error: set Advertising ID declaration to "No" in App Content.

### Technical Warnings & Optimization Backlog (Google Play Console):
- [ ] **Opt.1 (Deobfuscation / R8):** Console warns about missing `mapping.txt`. Currently `isMinifyEnabled = false` to guarantee stability of on-device Gemini Nano and Koog during initial testing. Future task: Enable R8 with `proguard-rules.pro` and generate mapping file for production release (compliance deadline: Feb 2027).
- [ ] **Opt.2 (Native C++ Symbols):** Console warns about native code without debug symbols (`.so` from SQLite/SQLDelight and NPU). Future task: Add `ndk { debugSymbolLevel = "SYMBOL_TABLE" }` in `buildTypes.release` inside `build.gradle.kts` to package symbols automatically.

## Phase 2: UI Interactivity & Stub Elimination
- [x] 2.1 Implement message actions in `BubbleAssistantMessage.kt` via `BubbleAssistantActionBar.kt` (6 containerless micro-actions: animated Copy via LocalClipboard, TTS Audio, Like, Dislike, native Share Sheet, and More options).
- [x] 2.2 Wire interactive buttons in `FloatingPromptBar.kt` (`+` context attachment button via `AttachContextBottomSheet` and `Mic` button for native voice dictation via `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`).
- [x] 2.3 Wire live audio button in `LiveAudioWaveOrSendButton.kt` with user notice for upcoming AICore Live availability.
- [x] 2.4 Wire `Bubble Local` selector in `BubbleTopAppBar.kt` and `MainScreen.kt` with `BubbleLocalInfoBottomSheet` explaining on-device Gemini Nano.
- [x] 2.5 Add visual feedback (Snackbar) to "Coming Soon" drawer items (`Focus mode`, `Apps`, `Challenges`) in `BubbleMenuDrawer.kt`.
- [x] 2.6 Provide interactive security info for the shield icon in `ProfileVaultTopBar.kt` via `VaultSecurityInfoDialog.kt`.
- [x] 2.7 Remove `"Simulate MCP"` test button for clean production release.

## Phase 3: Visual Identity & Data Protection
- [x] 3.1 Update launcher icon (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`, and mipmaps) with official Bubble vector and image assets (removing generic robot icon).
- [x] 3.2 Create `data_extraction_rules.xml` and `backup_rules.xml` in `androidApp/src/main/res/xml/` and link in `AndroidManifest.xml` (`android:dataExtractionRules` and `android:fullBackupContent`), protecting `bubble.db` from unauthorized cloud backups.
- [x] 3.3 Validate AppFunctions interface consistency (`readUserProfile` and `saveUserProfile`) in `bubble_app_function_service.xml`.

## Phase 4: Pre-Flight Verification & Google Play Policies
- [x] 4.1 Execute full test suite (`./gradlew testDebugUnitTest` - 100% passing).
- [x] 4.2 Pre-flight inspection of generated `androidApp-release.aab` (versionCode 20, versionName "1.2.1", targetSdk 36, signed with official keystore, SHA-256 verified `77:CF:...:C0:5C` via `verify-release-aab.sh`).
- [x] 4.3 Policy & privacy checklist:
  - Public Privacy Policy created in `docs/index.html` and `docs/privacy-policy.html` detailing Local-First AI, zero remote telemetry, local database backup exclusion, and no Advertising ID collection.
  - Official Google Play Console form responses prepared:
    - Advertising ID: "No".
    - Health apps declaration: "No, my app is not a health app and does not provide health-related features".
    - Data safety: Does not collect or share user data with third parties (100% on-device).
    - `PACKAGE_USAGE_STATS` permission: Strictly used locally via `UsageStatsManager` to compute screen time totals vs. offline potential.
