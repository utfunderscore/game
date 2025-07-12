plugins {
    id("com.gradleup.shadow") version "9.0.0-beta4"
    `java-library`
}

repositories {
    mavenCentral()
    maven {
        name = "utfMvn"
        url = uri("https://mvn.utf.lol/releases")
    }
    maven { url = uri("https://jitpack.io") }
    maven("https://repo.panda-lang.org/releases")
}

dependencies {

    compileOnly("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")
    compileOnly("com.github.TogAr2:MinestomPvP:126a5a00be")
    api(project(":core"))
    compileOnly("org.readutf.buildformat:common:1.0.18")

    api("dev.hollowcube:schem:2.0.0")
    api("dev.hollowcube:polar:1.14.5")
    api("org.slf4j:slf4j-api:2.0.16")

    api("net.bladehunt:kotstom:0.4.0-beta.0")
}


tasks.compileJava {
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}
