import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("io.github.oshai:kotlin-logging-jvm:5.1.4")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    api("io.github.revxrsal:lamp.common:4.0.0-beta.17")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    api(kotlin("reflect"))
    api("net.kyori:adventure-api:4.18.0")
    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")
    api("org.readutf.buildformat:common:1.0.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
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
    jvmToolchain(23)
}
