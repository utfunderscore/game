plugins {
    id("com.gradleup.shadow") version "9.0.0-beta4"
    `java-library`
    id("io.freefair.lombok") version "8.13.1"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    maven {
        name = "utfRepoReleases"
        url = uri("https://mvn.utf.lol/releases")
    }
}

dependencies {

    api("net.minestom:minestom-snapshots:1_21_5-69b9a5d844")
    api("org.readutf.arena:core:1.1.0")

    compileOnly("io.github.togar2:minestompvp:1.0")

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
