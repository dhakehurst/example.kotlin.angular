pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}

rootProject.name = file(".").name

include("server")
include("information")
include("client")

enableFeaturePreview("GRADLE_METADATA")