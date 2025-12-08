plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.qrshield.android"
    compileSdk = 35  // Android 16

    defaultConfig {
        applicationId = "com.qrshield.android"
        minSdk = 26
        targetSdk = 35  // Android 16
        versionCode = 2  // Incremented for release
        versionName = "1.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Resource configurations for localization
        resourceConfigurations += listOf("en", "es", "fr", "de", "ja", "zh", "ar")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
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
}
