# ProGuard rules for QR-SHIELD Android App
# Updated for Android 16 (API 35)

# =============================================================================
# Kotlin Metadata
# =============================================================================
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# =============================================================================
# Kotlin Coroutines (Android 16 optimized)
# =============================================================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.android.** { *; }
-dontwarn kotlinx.coroutines.**

# =============================================================================
# Jetpack Compose (Material 3)
# =============================================================================
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.lifecycle.** { *; }

# =============================================================================
# CameraX (1.4.0 - Android 16 Low Light Boost)
# =============================================================================
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
-keep class androidx.camera.core.** { *; }
-keep class androidx.camera.camera2.** { *; }
-keep class androidx.camera.lifecycle.** { *; }
-keep class androidx.camera.view.** { *; }

# =============================================================================
# Google ML Kit Barcode Scanning (17.3.0)
# =============================================================================
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# =============================================================================
# QR-SHIELD Domain Models
# =============================================================================
-keep class com.qrshield.model.** { *; }
-keep class com.qrshield.core.** { *; }
-keep class com.qrshield.scanner.** { *; }
-keep class com.qrshield.ui.** { *; }
-keep class com.qrshield.data.** { *; }

# =============================================================================
# Koin Dependency Injection
# =============================================================================
-keep class org.koin.** { *; }
-dontwarn org.koin.**
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}

# =============================================================================
# SQLDelight Generated Code
# =============================================================================
-keep class com.qrshield.db.** { *; }
-keep class app.cash.sqldelight.** { *; }

# =============================================================================
# Ktor Client (for network requests)
# =============================================================================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# =============================================================================
# Android 16: Predictive Back Animation Support
# =============================================================================
-keep class androidx.activity.** { *; }
-keep class * implements android.window.OnBackInvokedCallback { *; }

# =============================================================================
# Release Optimizations
# =============================================================================

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Optimization settings
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# Keep source file names for crash reporting
-renamesourcefileattribute SourceFile

# Android 16: R8 full mode
-allowaccessmodification
-mergeinterfacesaggressively
