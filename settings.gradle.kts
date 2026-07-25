pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.parchmentmc.org")
        maven("https://repo.codemc.io/repository/relativitymc/")
        mavenCentral()
        gradlePluginPortal()
    }
}

includeBuild("build-logic")
include("common")
include("fabric")
include("neoforge")

rootProject.name = "alloy-forgery"
