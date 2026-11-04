# Project specific ProGuard / R8 rules for Bubble (com.jesusdmedinac.bubble)

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# SQLDelight
-keep class com.jesusdmedinac.bubble.db.** { *; }
-keep interface app.cash.sqldelight.** { *; }

# AppFunctions & Android MCP
-keep class androidx.appfunctions.** { *; }
-keep class com.jesusdmedinac.bubble.appfunctions.** { *; }
-keepclassmembers class com.jesusdmedinac.bubble.appfunctions.** {
    @androidx.appfunctions.service.AppFunction <methods>;
}

# Koog AI Agent Framework & Coroutines
-keep class ai.koog.** { *; }
-dontwarn ai.koog.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# ML Kit GenAI
-keep class com.google.mlkit.genai.** { *; }
-dontwarn com.google.mlkit.genai.**

# Jetpack Compose & Navigation
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
