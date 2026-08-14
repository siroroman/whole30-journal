import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.detekt)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core.uiUistate)
            implementation(projects.shared.core.designSystem)
            implementation(projects.shared.core.utils)
            implementation(projects.shared.feature.dayEntry.domain)
            implementation(projects.shared.feature.dayEntry.presentation)
            implementation(projects.shared.feature.program.domain)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.compose.components.resources)

            implementation(libs.coil.compose)
            implementation(libs.coil.core)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
        }
        iosMain.dependencies {
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}

android {
    namespace = "dev.whole30journal.feature.daydetail.presentation"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }
}

compose.resources {
    packageOfResClass = "dev.whole30journal.feature.daydetail.presentation.generated.resources"
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
    debugImplementation(libs.compose.ui.tooling)

    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.rules.compose)
}
