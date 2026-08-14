# Whole30 Journal App

A Kotlin Multiplatform starter project meant to speed up the start of a new KMP app for Android and
iOS. It provides the shared ViewModel foundation (`shared/core/ui-uistate`) and the iOS/Koin bridge
(`iosApp/iosApp/Bridge`) for two code-sharing modes across the two platforms:

- Shared **Compose Multiplatform** UI (written once in `commonMain`), running natively on both
  platforms (wrapped in `ComposeUIViewController` on iOS).
- **Native** UI on each platform (Jetpack Compose / SwiftUI), driven by **the same** shared KMP
  `ViewModel` (observed on iOS via SKIE `for await`).

See [ARCHITECTURE.md](ARCHITECTURE.md) for a full walkthrough of both patterns with code, and
`scripts/create_kmp_feature_boilerplate.sh` to scaffold a new feature.

## Tech stack

Kotlin 2.4.0 · Compose Multiplatform 1.11.1 (Material3 1.9.0) · Koin 4.2.2 · SKIE 0.10.13 ·
kotlinx.coroutines 1.11.0 · androidx.lifecycle 2.11.0 · Android Gradle Plugin 9.3.1.

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
