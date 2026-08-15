# Whole30 Journal App

A Kotlin Multiplatform app for Android and iOS that tracks a Whole30 program: configure a start
date and duration, log one entry per day (energy / mood / sleep / cravings scores, meals with
photos, achievements, notes), and review progress and trends on the home screen. Everything is
stored locally in SQLDelight — there is no backend.

Every screen is a **Compose Multiplatform** screen written once in `commonMain` and rendered
natively on both platforms (wrapped in `ComposeUIViewController` on iOS). Each app module owns only
navigation, DI bootstrap, and platform plumbing — Jetpack Navigation Compose on Android, a SwiftUI
`NavigationStack` driven by the shared ViewModel's output events (observed via SKIE `for await`) on
iOS.

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full walkthrough and [AGENTS.md](AGENTS.md) for
build commands and conventions.

## Tech stack

Kotlin 2.4.10 · Compose Multiplatform 1.11.1 (Material3 1.9.0) · Koin 4.2.2 · SKIE 0.10.14 ·
SQLDelight 2.3.2 · Coil 3.5.0 · kotlinx.coroutines 1.11.0 · kotlinx-datetime 0.7.1 ·
androidx.lifecycle 2.11.0 · Android Gradle Plugin 9.3.1.

See [gradle/libs.versions.toml](gradle/libs.versions.toml) for the full, always up-to-date version
catalog.

## Running the app

### Android

```bash
./gradlew :androidApp:installDebug
```

### iOS

```bash
cd iosApp
xcodegen generate        # generates iosApp.xcodeproj from project.yml (not checked into git)
open iosApp.xcodeproj
```

Just run the `iosApp` scheme in Xcode — a Run Script build phase automatically runs
`./gradlew :shared:umbrella:embedAndSignAppleFrameworkForXcode` and embeds `SharedKit.framework`.

Also verified directly via CLI:
```bash
xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -sdk iphonesimulator -destination 'id=<simulator-udid>' build
```

## Tests

```bash
./gradlew jvmTest testDebugUnitTest   # what CI runs
./gradlew detekt
```
