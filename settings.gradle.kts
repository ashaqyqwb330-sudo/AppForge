pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "AppForge"
include(":app")
