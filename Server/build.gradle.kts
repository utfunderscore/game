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
