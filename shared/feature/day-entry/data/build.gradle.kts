plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
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
            implementation(projects.shared.feature.dayEntry.domain)
            implementation(projects.shared.core.database)
            implementation(libs.kotlinx.coroutines.core)
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
        // The default hierarchy template creates iosTest but doesn't attach it as a parent of
        // the per-architecture leaf test source sets - wire that up explicitly.
        val iosArm64Test by getting { dependsOn(iosTest.get()) }
        val iosSimulatorArm64Test by getting { dependsOn(iosTest.get()) }
    }
}

android {
    namespace = "dev.whole30journal.feature.dayentry.data"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }
}

// AbstractTestTask (not Test) so this also covers the Kotlin/Native iosArm64Test /
// iosSimulatorArm64Test tasks, which don't extend the JVM-specific Test task type.
tasks.withType<AbstractTestTask>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
    }
}
