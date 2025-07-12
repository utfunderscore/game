plugins {
    `maven-publish`
    `java-library`
    id("com.diffplug.spotless") version "7.0.4"
}

group = "org.readutf.arena"
version = "dev"

repositories {
    mavenCentral()
}

dependencies {
}


subprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    java {
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                groupId = project.group as String
                artifactId = project.name
                version = project.version as String
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "utfMvn"
                url = uri("https://mvn.utf.lol/releases")
                credentials {
                    username = System.getenv("UTF_MVN_USER") ?: findProperty("utfMvnUser")?.toString() ?: ""
                    password = System.getenv("UTF_MVN_PASS") ?: findProperty("utfMvnPass")?.toString() ?: ""
                }
            }

        }
    }
}

tasks.test {
    useJUnitPlatform()
}
