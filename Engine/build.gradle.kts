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

    api("io.github.oshai:kotlin-logging-jvm:5.1.4")

    // Add jackson databind
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")

    api("dev.hollowcube:polar:1.11.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    implementation(kotlin("reflect"))

    testImplementation("net.minestom:minestom-snapshots:b0bad7e180")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
