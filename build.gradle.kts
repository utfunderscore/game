import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.net.URI

plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.readutf.game"
version = "1.0-SNAPSHOT"

subprojects {

    repositories {
        mavenLocal()
        maven { url = uri("https://repo.readutf.org/releases") }
        maven { url = uri("https://www.jitpack.io") }
    }
}

repositories {
    mavenLocal()
    maven {
        url = URI("https://repo.readutf.org/releases/")
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

// optional: if you're using Kotlin
tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        javaParameters = true
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
