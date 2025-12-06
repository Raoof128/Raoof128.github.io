plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
}

allprojects {
    group = "com.qrshield"
    version = "1.0.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
