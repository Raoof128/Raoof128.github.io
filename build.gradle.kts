plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kover)
}

allprojects {
    group = "com.qrshield"
    version = "1.0.0"
}

// Kover configuration - only include common module to avoid Android variant conflicts
dependencies {
    kover(project(":common"))
}

