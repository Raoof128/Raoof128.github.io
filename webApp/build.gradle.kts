plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            binaries.executable()
        }
    }

    // Wasm target - Enabled with SQLDelight 2.2.1 which adds wasmJs support
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                cssSupport { enabled.set(true) }
            }
            binaries.executable()
        }
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

// Fix duplicate webApp.js issue in distribution task
tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Sync>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
