plugins {
    `maven-publish`
    `java-library`
    id("com.diffplug.spotless") version "7.0.4"
}

group = "org.readutf.arena"
version = "1.2.3"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.17.0") // Or use other formatters like eclipse, prettier, etc.
        trimTrailingWhitespace()
        endWithNewline()
    }

    // Optional: format Gradle Kotlin scripts too
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint()
    }
}

subprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    java {
//        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = "org.readutf.arena"
                version = rootProject.version.toString()
                artifactId = project.name

                from(components["java"])
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
