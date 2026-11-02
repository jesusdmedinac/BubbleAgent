import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.sqldelight)
}

fun readEnvProperties(): Properties {
  val properties = Properties()
  val envFile = rootProject.file("env.properties")
  if (envFile.exists()) {
    properties.load(envFile.inputStream())
  }
  return properties
}

android {
  namespace = "com.jesusdmedinac.bubble"
  compileSdk = 37
  defaultConfig {
    applicationId = "com.jesusdmedinac.bubble"
    minSdk = 26
    targetSdk = 36
    versionCode = 19
    versionName = "1.2.0"
  }

  val env = readEnvProperties()

  signingConfigs {
    create("release") {
      val keystorePath = env.getProperty("KEYSTORE_PATH_RELEASE")
      if (!keystorePath.isNullOrBlank()) {
        storeFile = rootProject.file(keystorePath)
        storePassword = env.getProperty("KEYSTORE_PASSWORD") ?: ""
        keyAlias = env.getProperty("KEY_ALIAS") ?: ""
        keyPassword = env.getProperty("KEY_PASSWORD") ?: ""
      }
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    aidl = false
    buildConfig = false
    shaders = false
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

kotlin {
  jvmToolchain(17)
}

sqldelight {
  databases {
    create("BubbleDatabase") {
      packageName.set("com.jesusdmedinac.bubble.db")
    }
  }
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)
  androidTestImplementation(composeBom)

  // Core Android dependencies
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)

  // Arch Components
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  // Tooling
  debugImplementation(libs.androidx.compose.ui.tooling)
  // Instrumented tests
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  // Local tests: jUnit, coroutines, Android runner
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)

  // Instrumented tests: jUnit rules and runners
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.espresso.core)

  // Navigation
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.navigation3)

  // ML Kit GenAI (Gemini Nano)
  implementation(libs.mlkit.genai.prompt)
  implementation(libs.mlkit.genai.agent)

  // AppFunctions (Android MCP Server)
  implementation(libs.androidx.appfunctions)
  ksp(libs.androidx.appfunctions.compiler)

  // SQLDelight Persistence
  implementation(libs.sqldelight.android.driver)
  implementation(libs.sqldelight.coroutines.extensions)

  // Koog AI Agent Framework (JetBrains)
  implementation(libs.koog.agents)
}
