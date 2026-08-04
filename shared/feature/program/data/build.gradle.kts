import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.detekt)
}

kotlin {
    // Restores the standard source-set edges (e.g. iosSimulatorArm64Test -> iosTest) that adding
    // the custom jvmAndIosTest dependsOn edges below would otherwise silently disable project-wide.
    applyDefaultHierarchyTemplate()

    androidTarget()
    jvm() // repository tests run here via SQLDelight's JDBC driver - see jvmTest
    // linkerOpts: SQLDelight's native-driver (sqliter) needs libsqlite3 linked explicitly for
    // standalone test binaries - app binaries get it for free via Xcode's own linking.
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
        // Shared between jvmTest and iosTest only (not androidUnitTest, since Android's
        // DatabaseDriverFactory actual needs a Context that common test code can't supply).
        val jvmAndIosTest by creating {
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

// AbstractTestTask (not Test) so this also covers the Kotlin/Native iosArm64Test /
// iosSimulatorArm64Test tasks, which don't extend the JVM-specific Test task type.
tasks.withType<AbstractTestTask>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
    }
}
