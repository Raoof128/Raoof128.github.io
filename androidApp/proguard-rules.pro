# ProGuard rules for QR-SHIELD Android App

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep QR-SHIELD models
-keep class com.qrshield.model.** { *; }
-keep class com.qrshield.core.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Keep Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep SQLDelight generated code
-keep class com.qrshield.db.** { *; }

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Optimization
-optimizationpasses 5
-allowaccessmodification
-dontpreverify
