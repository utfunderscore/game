plugins {
    id("com.gradleup.shadow") version "9.0.0-beta4"
    `java-library`
}

repositories {
    mavenCentral()
    maven { url = uri("https://mvn.utf.lol/releases") }
    maven { url = uri("https://jitpack.io") }
    maven("https://repo.panda-lang.org/releases")
}

dependencies {

    compileOnly("net.minestom:minestom:2025.10.31-1.21.10")
    compileOnly("com.github.TogAr2:MinestomPvP:56a831b41c")
    api(project(":core"))
    compileOnly("org.readutf.buildformat:common:v2.0.10")

    api("dev.hollowcube:schem:1d3ecd1a62")
    api("dev.hollowcube:polar:1.15.0")
    api("org.slf4j:slf4j-api:2.0.16")
    api("it.unimi.dsi:fastutil:8.5.18")

    api("net.bladehunt:kotstom:0.4.0-beta.0")
}


java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(25)) // Keep inline with the latest Minestom version
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}
