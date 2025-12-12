plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kover)
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

allprojects {
    group = "com.qrshield"
    version = "1.1.1"
}

// Kover configuration - only include common module to avoid Android variant conflicts
dependencies {
    kover(project(":common"))
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
    parallel = true
    
    // Source sets to analyze
    source.setFrom(
        files(
            "common/src/commonMain/kotlin",
            "common/src/androidMain/kotlin",
            "common/src/iosMain/kotlin",
            "common/src/desktopMain/kotlin",
            "common/src/jsMain/kotlin",
            "androidApp/src/main/kotlin",
            "desktopApp/src/desktopMain/kotlin",
            "webApp/src/jsMain/kotlin"
        )
    )
}

// Configure detekt reports
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        sarif.required.set(true)
        txt.required.set(false)
    }
}

