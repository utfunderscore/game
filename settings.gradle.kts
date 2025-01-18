plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "minestom-engine"
include("Engine")
include("Server")
include("Minestom")
include("MinestomServer")
