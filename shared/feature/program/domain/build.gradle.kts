import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
}

kotlin {
    androidTarget()
    jvm()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "dev.whole30journal.feature.program.domain"
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

tasks.withType<AbstractTestTask>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
    }
}
