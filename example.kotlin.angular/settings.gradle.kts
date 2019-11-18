pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = file(".").name

include("information")
include("user-api")
include("gui2core")
include("websocketClient")
include("server")
include("client")

enableFeaturePreview("GRADLE_METADATA")