import java.net.URI

plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.readutf.game"
version = "1.0-SNAPSHOT"

subprojects {

    repositories {
        mavenLocal()
        maven {
            url = URI("https://reposilite.readutf.org/releases")
        }
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
