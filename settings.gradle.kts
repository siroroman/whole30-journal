pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "whole30-journal"

include(":androidApp")

// Core
include(":shared:core:ui-uistate")
include(":shared:core:network")

// Feature: example (shared Compose Multiplatform UI + shared VM)
include(":shared:feature:example:domain")
include(":shared:feature:example:data")
include(":shared:feature:example:presentation")

// Umbrella - combines everything into one XCFramework for iOS
include(":shared:umbrella")
