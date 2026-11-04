plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.sqldelight)
}

kotlin {
  android {
    namespace = "com.jesusdmedinac.bubble.shared"
    compileSdk = 37
    minSdk = 26

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
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
      implementation(libs.kotlinx.coroutines.test)
    }
    androidMain.dependencies {
      implementation(libs.sqldelight.android.driver)
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

sqldelight {
  databases {
    create("BubbleDatabase") {
      packageName.set("com.jesusdmedinac.bubble.db")
      schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
      verifyMigrations.set(true)
    }
  }
}
