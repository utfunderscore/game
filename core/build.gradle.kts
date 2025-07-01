plugins {
    `java-library`
}

repositories {
    mavenCentral()
    maven { url = uri("https://mvn.utf.lol/releases") }
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    api("io.github.revxrsal:lamp.common:4.0.0-beta.17")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    api("net.kyori:adventure-api:4.18.0")
    api("org.readutf.buildformat:common:1.0.17")

    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.0")
}

tasks.withType<JavaCompile> {
    // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform()
}