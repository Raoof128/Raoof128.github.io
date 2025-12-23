plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

import java.util.Properties
import java.io.FileInputStream

android {
    namespace = "com.qrshield.android"
    compileSdk = 35  // Android 16

    defaultConfig {
        applicationId = "com.qrshield.android"
        minSdk = 26
        targetSdk = 35  // Android 16
        versionCode = 8  // v1.17.10 release
        versionName = "1.17.10"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    // Resource configurations for localization - all 15 supported languages
    androidResources {
        localeFilters += listOf(
            "en",  // English (default)
            "de",  // German
            "es",  // Spanish
            "fr",  // French
            "it",  // Italian
            "pt",  // Portuguese
            "ru",  // Russian
            "zh",  // Chinese
            "ja",  // Japanese
            "ko",  // Korean
            "hi",  // Hindi
            "ar",  // Arabic
            "tr",  // Turkish
            "vi",  // Vietnamese
            "in",  // Indonesian
            "th"   // Thai
        )
    }

    signingConfigs {
        create("release") {
            val keyStoreFile = project.rootProject.file("keystore.properties")
            if (keyStoreFile.exists()) {
                val properties = Properties()
                properties.load(FileInputStream(keyStoreFile))
                
                storeFile = file(properties.getProperty("storeFile"))
                storePassword = properties.getProperty("storePassword")
                keyAlias = properties.getProperty("keyAlias")
                keyPassword = properties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            
            val keyStoreFile = project.rootProject.file("keystore.properties")
            if (keyStoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Note: composeOptions block removed - handled by kotlin.compose plugin in Kotlin 2.0+
}

dependencies {
    implementation(project(":common"))
    
    // Compose
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(compose.ui)
    implementation(libs.androidx.activity.compose)
    
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)
    
    // Lifecycle (fixes LocalLifecycleOwner deprecation)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Camera
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // ML Kit
    implementation(libs.mlkit.barcode)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    
    // SplashScreen API (Android 12+)
    implementation("androidx.core:core-splashscreen:1.0.1")
    
    // Glance App Widget (Android 16)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    
    // Photo Picker for gallery scanning
    implementation(libs.androidx.photo.picker)
    
    // Baseline Profile installer (enables profile-guided optimization)
    implementation(libs.androidx.profileinstaller)
    
    // Material Icons Extended for accessibility
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    
    // Testing
    testImplementation(libs.kotlin.test)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")
    
    // Compose UI Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")
    
    // Testing utilities
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Benchmark for Baseline Profile (optional - comment out if not needed)
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.2.3")
}
