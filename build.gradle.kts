plugins {
    `maven-publish`
    `java-library`
    id("com.diffplug.spotless") version "7.0.4"
}

group = "org.readutf.arena"
version = "1.2.7"

repositories {
    mavenCentral()
}

dependencies {
}


subprojects {

    if (project.name == "plugin") {
        return@subprojects
    }

    apply(plugin = "maven-publish")
    apply(plugin = "java-library")

    version = rootProject.version
    group = rootProject.group

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "utfMvn"
                url = uri("https://mvn.utf.lol/releases")
                credentials {
                    username = System.getenv("UTF_MVN_USER") ?: findProperty("utfMvnUser") as String? ?: "readutf"
                    password = System.getenv("UTF_MVN_PASS") ?: findProperty("utfMvnPass") as String? ?: "readutf"
                }
            }

        }
    }
}

tasks.test {
    useJUnitPlatform()
}
