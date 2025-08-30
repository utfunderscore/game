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

    compileOnly("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")
    compileOnly("com.github.TogAr2:MinestomPvP:56a831b41c")
    api(project(":core"))
    compileOnly("org.readutf.buildformat:common:1.0.26")

    api("dev.hollowcube:schem:2.0.0")
    api("dev.hollowcube:polar:1.14.5")
    api("org.slf4j:slf4j-api:2.0.16")

    api("net.bladehunt:kotstom:0.4.0-beta.0")
}


java.toolchain {
    languageVersion.set(JavaLanguageVersion.of(21)) // Keep inline with the latest Minestom version
}

tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}
