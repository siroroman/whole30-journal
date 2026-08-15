import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget()
    jvm()
    iosArm64 {
        binaries.getTest("DEBUG").linkerOpts += "-lsqlite3"
    }
    iosSimulatorArm64 {
        binaries.getTest("DEBUG").linkerOpts += "-lsqlite3"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.feature.program.domain)
            implementation(projects.shared.core.database)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.koin.core)
        }
        val jvmAndIosTest = create("jvmAndIosTest") {
            dependsOn(commonTest.get())
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmTest.get().dependsOn(jvmAndIosTest)
        jvmTest.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        iosTest.get().dependsOn(jvmAndIosTest)
        iosTest.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

android {
    namespace = "dev.whole30journal.feature.program.data"
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
