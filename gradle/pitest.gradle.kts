// Pitest (Mutation Testing) Gradle Configuration
// This file adds mutation testing support for JVM targets
//
// Apply this in build.gradle.kts with:
//   apply(from = "gradle/pitest.gradle.kts")

// Note: Pitest plugin for Kotlin requires special setup due to KMP
// This configuration is for manual pitest runs on JVM code

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.15.0")
    }
}

// For JVM-only modules, apply:
// plugins {
//     id("info.solidsoft.pitest") version "1.15.0"
// }
//
// pitest {
//     targetClasses.set(listOf("com.raouf.mehrguard.*"))
//     targetTests.set(listOf("com.raouf.mehrguard.*Test"))
//     mutators.set(listOf("STRONGER"))
//     outputFormats.set(listOf("HTML", "XML"))
//     threads.set(4)
//     timestampedReports.set(false)
//     mutationThreshold.set(60)
//     coverageThreshold.set(80)
// }

println("Pitest configuration loaded. Use './gradlew pitest' on JVM modules.")
