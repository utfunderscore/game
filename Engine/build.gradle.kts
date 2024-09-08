plugins {
    kotlin("jvm")
    id("java-library")
}

group = "com.readutf.game"
version = "1.0-SNAPSHOT"

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    compileOnly("net.minestom:minestom-snapshots:b0bad7e180")

    api("dev.hollowcube:schem:1.3.1")

    implementation("io.github.oshai:kotlin-logging-jvm:5.1.4")

    // Add jackson databind
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
