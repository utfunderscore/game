import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    id("java-library")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Add jackson databind
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    api("io.github.oshai:kotlin-logging-jvm:5.1.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    api("io.github.revxrsal:lamp.common:4.0.0-beta.17")
    api("io.github.revxrsal:lamp.minestom:4.0.0-beta.17")

    api("com.github.TogAr2:MinestomPvP:04180ddf9a")

    api("com.michael-bull.kotlin-result:kotlin-result:2.0.1")

    api(kotlin("reflect"))

    api("net.kyori:adventure-api:4.18.0")
    api("net.kyori:adventure-text-serializer-legacy:4.18.0")

    testImplementation("net.minestom:minestom-snapshots:b80c799750")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

tasks {
    withType<JavaCompile> {
        // Preserve parameter names in the bytecode
        options.compilerArgs.add("-parameters")
    }

    withType<KotlinJvmCompile> {
        compilerOptions {
            javaParameters = true
        }
    }
    test {
        useJUnitPlatform()
    }
}
kotlin {
    jvmToolchain(23)
}
