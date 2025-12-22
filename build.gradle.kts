plugins {
    `maven-publish`
    `java-library`
    id("com.diffplug.spotless") version "7.0.4"
}


group = "org.readutf.arena"
version = System.getenv("GITHUB_REF_NAME") ?: "dev"

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

    val sourceSets = extensions.getByType(org.gradle.api.tasks.SourceSetContainer::class)

    val sourcesJar by tasks.registering(org.gradle.api.tasks.bundling.Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").allSource)
    }

    val javadocTask = tasks.named("javadoc", Javadoc::class)
    val javadocJar by tasks.registering(org.gradle.api.tasks.bundling.Jar::class) {
        archiveClassifier.set("javadoc")
        from(javadocTask.map { it.destinationDir })
        dependsOn(javadocTask)
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(sourcesJar.get())
                artifact(javadocJar.get())
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
