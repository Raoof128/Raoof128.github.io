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

    @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
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
        
        val wasmJsMain by creating {
            dependencies {
                implementation(project(":common"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
