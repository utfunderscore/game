import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.3"
}

group = "com.readutf.game"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(project(":Engine"))

    implementation("net.minestom:minestom-snapshots:b0bad7e180")

    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.0")

    // Add influxdb for metrics
    implementation("com.influxdb:influxdb-client-kotlin:6.6.0")

    implementation("io.github.revxrsal:lamp.cli:4.0.0-beta.17")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")

    implementation("team.unnamed:creative-api:1.7.3")

    // Serializer for Minecraft format (ZIP / Folder)
    implementation("team.unnamed:creative-serializer-minecraft:1.7.3")

    // Resource Pack server
    implementation("team.unnamed:creative-server:1.7.3")

    implementation("org.readutf.glyphs:api:1.0.0")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

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
