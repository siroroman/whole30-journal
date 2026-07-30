import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.skie)
    alias(libs.plugins.detekt)
}

skie {
    swiftBundling {
        enabled = true
    }
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries.framework {
            baseName = "SharedKit"
            isStatic = true

            // Only project modules can be exported (flattened into the framework's Swift
            // namespace) - third-party deps like Koin/coroutines stay linked-in but un-exported,
            // same as Rohlik's umbrella module.
            export(projects.shared.core.uiUistate)
            export(projects.shared.feature.example.domain)
            export(projects.shared.feature.example.presentation)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core.uiUistate)
            api(projects.shared.feature.example.domain)
            api(projects.shared.feature.example.presentation)
            implementation(projects.shared.feature.example.data)
            implementation(projects.shared.core.network)

            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "dev.whole30journal.umbrella"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom(kotlin.sourceSets.flatMap { it.kotlin.srcDirs }.filterNot { "/build/" in it.path })
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.rules.compose)
}
