rootProject.name = "ksync"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

include("client")
include("codegen")
include("core")
include("server")

include("example:backend")
include("example:composeApp")
include("example:shared")
