plugins {
    kotlin("jvm")
}

group = "com.qrshield"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Kotlin standard library
    implementation(kotlin("stdlib"))
    
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

