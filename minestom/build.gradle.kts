plugins {
    id("com.gradleup.shadow") version "9.0.0-beta4"
    `java-library`
    id("io.freefair.lombok") version "8.13.1"
}

repositories {
    mavenCentral()
    maven {
        name = "utfMvn"
        url = uri("https://mvn.utf.lol/releases")
    }
    maven { url = uri("https://jitpack.io") }

}

dependencies {

    api("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")
    api("org.readutf.arena:core:1.1.0")
    compileOnly("com.github.TogAr2:MinestomPvP:126a5a00be")
    api(project(":core"))

    api("dev.hollowcube:schem:2.0.0")
    api("dev.hollowcube:polar:1.14.0")
    api("org.slf4j:slf4j-api:2.0.16")

    api("net.bladehunt:kotstom:0.4.0-beta.0")
}

tasks {
    withType<JavaCompile> {
        // Preserve parameter names in the bytecode
        options.compilerArgs.add("-parameters")
    }
}

tasks.test {
    useJUnitPlatform()
}
