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
include(":shared:core:database")

// Feature: day-entry (domain + data only - no presentation layer yet)
include(":shared:feature:day-entry:domain")
include(":shared:feature:day-entry:data")

// Feature: program (domain + data only - no presentation layer yet)
include(":shared:feature:program:domain")
include(":shared:feature:program:data")

// Umbrella - combines everything into one XCFramework for iOS
include(":shared:umbrella")
