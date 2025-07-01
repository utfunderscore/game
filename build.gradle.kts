plugins {
    `maven-publish`
    `java-library`
    id("com.diffplug.spotless") version "7.0.4"
}

group = "org.readutf.arena"
version = "1.2.5"

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
                from(components["java"])
            }
        }

        repositories {
            maven {
                name = "utfMvn"
                url = uri("https://mvn.utf.lol/releases")
                credentials {
                    username = System.getenv("UTF_MVN_USER")
                    password = System.getenv("UTF_MVN_PASS")
                }
            }

        }
    }
}

tasks.test {
    useJUnitPlatform()
}
