val ktor_version="1.5.0"

plugins {
    kotlin("multiplatform") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"
}

group = "site.pegasis.ta.fetch.control"
version = "1.0"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val tacontrolTarget = when {
        hostOs == "Mac OS X" -> macosX64("tacontrol")
        hostOs == "Linux" -> linuxX64("tacontrol")
        isMingwX64 -> mingwX64("tacontrol")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    tacontrolTarget.apply {
        binaries {
            staticLib()
            executable {
                entryPoint = "main"
            }
        }
    }
    sourceSets {
        val tacontrolMain by getting{
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-curl:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
            }
        }
        val tacontrolTest by getting
    }
}
