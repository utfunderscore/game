import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.1.0"
}

group = "org.readutf.arena"
version = "1.0.0"

subprojects {

    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java")
    apply(plugin = "kotlin")

    kotlin {
        jvmToolchain(23)
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            javaParameters = true
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
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
