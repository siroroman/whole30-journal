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
include(":shared:core:design-system")
include(":shared:core:utils")

// Feature: day-entry (domain + data only - no presentation layer yet)
include(":shared:feature:day-entry:domain")
include(":shared:feature:day-entry:data")

include(":shared:feature:program:domain")
include(":shared:feature:program:data")

// Feature: home (presentation only - hardcoded UI data, no domain/data layer yet)
include(":shared:feature:home:presentation")

// Umbrella - combines everything into one XCFramework for iOS
include(":shared:umbrella")
