plugins {
    kotlin("jvm")
}

group = "com.raouf.mehrguard"
version = "2.0.36"

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

