import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val kotlin_coroutines_version: String by project

plugins {
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    implementation("org.slf4j:slf4j-nop:1.7.30")
    implementation("io.fluidsonic.mongo:fluid-mongo:1.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlin_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlin_coroutines_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation(files("libs/ktor-client-cio-jvm-$ktor_version.jar"))
//    implementation("io.ktor:ktor-client-cio-jvm:$ktor_version")
    implementation("io.ktor:ktor-network-tls:$ktor_version")
    implementation("io.ktor:ktor-network-tls-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-cio:$ktor_version")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("info.picocli:picocli:4.1.4")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("com.google.firebase:firebase-admin:6.12.1")
    implementation("net.sourceforge.htmlunit:htmlunit:2.45.0")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")

group = "site.pegasis"
version = "BN60"
description = "fetch-ta-data"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(14))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        languageVersion = "1.4"
        jvmTarget = "14"
        jdkHome = "/usr/lib/jvm/java-14-j9"
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveFileName.set("fetch_ta_data.jar")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "site.pegasis.ta.fetch.MainKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
