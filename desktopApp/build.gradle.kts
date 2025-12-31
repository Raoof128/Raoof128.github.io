import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose)
}

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    
    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.koin.core)
                implementation(libs.zxing.core)
                implementation(libs.zxing.javase)
            }
        }
        
        val desktopTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.raouf.mehrguard.desktop.MainKt"
        
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MehrGuard"
            packageVersion = "1.2.0"
            
            macOS {
                bundleID = "com.raouf.mehrguard.desktop"
                iconFile.set(project.file("icons/icon.icns"))
            }
            
            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "Mehr Guard"
            }
            
            linux {
                iconFile.set(project.file("icons/icon.png"))
            }
        }
    }
}
