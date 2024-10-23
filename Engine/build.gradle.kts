import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    id("java-library")
}

group = "com.readutf.game"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    compileOnly("net.minestom:minestom-snapshots:b0bad7e180")

    // Add jackson databind
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("dev.hollowcube:schem:dev")
    api("io.github.oshai:kotlin-logging-jvm:5.1.4")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    api("dev.hollowcube:polar:1.11.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
    api("io.github.revxrsal:lamp.common:4.0.0-beta.17")
    api("io.github.revxrsal:lamp.minestom:4.0.0-beta.17")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    api("com.github.TogAr2:MinestomPvP:0a5f94bcd7")

    implementation(kotlin("reflect"))

    testImplementation("net.minestom:minestom-snapshots:b0bad7e180")
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
    jvmToolchain(21)
}
