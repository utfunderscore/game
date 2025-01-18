plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation(project(":Engine"))

    implementation("net.minestom:minestom-snapshots:b80c799750")

    implementation("dev.hollowcube:polar:1.12.1")

    api("dev.hollowcube:schem:dev")
}

tasks.test {
    useJUnitPlatform()
}
