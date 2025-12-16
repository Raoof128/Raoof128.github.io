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

    // Wasm target - DISABLED: common module dependencies don't fully support wasmJs yet
    // @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class)
    // wasmJs {
    //     browser {
    //         commonWebpackConfig {
    //             cssSupport { enabled.set(true) }
    //         }
    //         binaries.executable()
    //     }
    // }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        
        // wasmJsMain - DISABLED
        // val wasmJsMain by getting {
        //     dependencies {
        //         implementation(project(":common"))
        //         implementation(libs.kotlin.stdlib)
        //         implementation(libs.kotlinx.coroutines.core)
        //     }
        // }
    }
}
