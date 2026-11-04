plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.sqldelight)
}

kotlin {
  android {
    namespace = "com.jesusdmedinac.bubble.shared"
    compileSdk = 37
    minSdk = 26

    // Packages composeResources (the logo) into the app; libraries skip Android resources by default.
    androidResources.enable = true

    withHostTest {}
  }

  iosArm64()
  iosSimulatorArm64()

  jvmToolchain(17)

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.coroutines.core)
      api(libs.sqldelight.coroutines.extensions)
      api(libs.json.to.compose)

      // Shared UI
      api(libs.cmp.runtime)
      api(libs.cmp.foundation)
      api(libs.cmp.ui)
      api(libs.cmp.material3)
      implementation(libs.cmp.material.icons.extended)
      implementation(libs.cmp.components.resources)
      implementation(libs.cmp.ui.tooling.preview)
      api(libs.jetbrains.lifecycle.viewmodel.compose)
      implementation(libs.jetbrains.lifecycle.runtime.compose)
      implementation(libs.jetbrains.navigation3.ui)
      implementation(libs.jetbrains.navigationevent.compose)
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }
    androidMain.dependencies {
      implementation(libs.sqldelight.android.driver)
      implementation(libs.androidx.activity.compose)
    }
    iosMain.dependencies {
      implementation(libs.sqldelight.native.driver)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.junit)
      implementation(libs.sqldelight.sqlite.driver)
    }
  }
}

compose.resources {
  packageOfResClass = "com.jesusdmedinac.bubble.resources"
}

dependencies {
  // Lets Android Studio render the @Preview functions in commonMain.
  "androidRuntimeClasspath"(libs.androidx.compose.ui.tooling)
}

sqldelight {
  databases {
    create("BubbleDatabase") {
      packageName.set("com.jesusdmedinac.bubble.db")
      schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
      verifyMigrations.set(true)
    }
  }
}
