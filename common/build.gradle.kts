plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kover)
}

// SQLDelight configuration
sqldelight {
    databases {
        create("QRShieldDatabase") {
            packageName.set("com.qrshield.db")
        }
    }
}

kotlin {
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // Desktop JVM target
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }
    
    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // JS/Web target
    js(IR) {
        browser {
            // Disable browser tests - backtick function names (Kotlin idiom)
            // are incompatible with JavaScript identifier naming rules
            testTask {
                enabled = false
            }
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                // SQLDelight runtime
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.android)
                implementation(libs.mlkit.barcode)
                implementation(libs.androidx.camera.core)
                implementation(libs.androidx.camera.camera2)
                implementation(libs.androidx.camera.lifecycle)
                implementation(libs.androidx.camera.view)
                // SQLDelight Android driver
                implementation(libs.sqldelight.android)
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(libs.zxing.core)
                implementation(libs.zxing.javase)
                implementation(compose.desktop.currentOs)
                // SQLDelight JVM driver
                implementation(libs.sqldelight.jvm)
            }
        }
        
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                // SQLDelight Native driver
                implementation(libs.sqldelight.native)
            }
        }
        
        val jsMain by getting {
            dependencies {
                // SQLDelight Web driver
                implementation(libs.sqldelight.web)
            }
        }
    }
}

android {
    namespace = "com.qrshield.common"
    compileSdk = 35  // Android 16
    
    defaultConfig {
        minSdk = 26
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
