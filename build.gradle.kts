plugins {
    kotlin("jvm") version "2.1.0"
    `maven-publish`
    `java-library`
}

group = "org.readutf.arena"
version = "1.1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
}

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")

    java {
        withJavadocJar()
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
kotlin {
    jvmToolchain(21)
}
