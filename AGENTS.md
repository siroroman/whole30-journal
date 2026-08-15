# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project Overview

A Kotlin Multiplatform starter app providing the shared ViewModel foundation and the iOS/Koin
bridge for two code-sharing modes between Android and iOS. No example feature is checked in — use
the boilerplate script below to scaffold one — see
[ARCHITECTURE.md](ARCHITECTURE.md) for a full walkthrough of both patterns with code, and
[README.md](README.md) for the pitch.

## Build and Development Commands

### Android
```bash
./gradlew :androidApp:installDebug   # build + install debug APK
./gradlew build                      # full build, all targets
./gradlew clean                      # clean build outputs
```

### iOS
```bash
cd iosApp && xcodegen generate && open iosApp.xcodeproj
```
Then run the `iosApp` scheme in Xcode — a Run Script build phase automatically runs
`./gradlew :shared:umbrella:embedAndSignAppleFrameworkForXcode` and embeds `SharedKit.framework`.
`iosApp.xcodeproj` is generated from `project.yml` and is not checked into git.

There is no automated test suite yet — verify changes by building and running the app on both
platforms. Static analysis: Detekt (`./gradlew detekt`) for Kotlin, SwiftLint (Xcode Run Script
build phase, config at `config/swiftlint/.swiftlint.yml`) for iOS.

## Architecture Overview

Clean-ish layering in shared feature modules (`domain → data → presentation`), MVI on
presentation, Koin for DI. Single app, single market, no product flavors — this is a small
reference project, not a production multi-market app (see "Deliberate Simplifications" below). See
[ARCHITECTURE.md](ARCHITECTURE.md) for the two sharing patterns this layering supports.

## Core Modules

- `shared:core:ui-uistate` — the shared-VM foundation: MVI contract (`UiStateAware` /
  `UiActionAware`), `StateFlowViewModel`. See ARCHITECTURE.md.
- `shared:umbrella` — combines all shared feature modules into the `SharedKit` XCFramework for
  iOS; hosts `KoinIOS` (Koin bootstrap for iOS) and `appModules` (the one list of Koin modules both
  platforms start from — currently just `networkModule`, until a feature is scaffolded).
- `androidApp` — Jetpack Compose app; starts Koin in `Whole30JournalApp` (`Application.onCreate`).
  `App.kt` currently renders an empty `Scaffold` — no feature is wired in yet.
- `iosApp` — SwiftUI app; `ContentView.swift` currently renders a placeholder — no feature is wired
  in yet. See "iOS ↔ KMP Integration" below for the surviving bridge infrastructure
  (`KoinStarter.swift`/`KoinResolver.swift`, `Core/ComposeViewController.swift`).

## Shared ViewModel Pattern (the core mechanic)

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full pattern with code. In short:
- Each feature defines an MVI contract: `object <Feature>Contract { UiData, UiAction, UiEvent,
  OutputEvent }`.
- `StateFlowViewModel` (`shared/core/ui-uistate`) is a plain class: a real
  `androidx.lifecycle.ViewModel` on Android, the same class instantiated as a plain Kotlin object
  owned by Swift on iOS — no expect/actual split needed, since `androidx.lifecycle.ViewModel` and
  `viewModelScope` are ordinary KMP APIs on both platforms. It holds a `MutableStateFlow` directly
  and exposes it as `state`, which is what lets the exact same VM subclass run unmodified on both
  platforms.
- Feature VMs extend `StateFlowViewModel` — use its `updateUiData {}` / `updateIsLoading()` /
  `updateUiEvents {}` / `emitOutputEvent()` helpers instead of touching the state flow directly.
- `UiEvent`s are transient and consumed once via `onUiEventConsumed` (e.g. a snackbar).
  `OutputEvent`s are one-shot side effects delivered over a `SharedFlow`, never replayed.

## Localization

Strings live per-feature in
`<feature>/presentation/src/commonMain/composeResources/values(-<lang>)/strings.xml` (Compose
Multiplatform Resources, the official JetBrains library - no extra dependency beyond what's already
in use). Rule of thumb: `stringResource()` from a composable (`ui/*Screen.kt`), the suspend
`getString()` from a plain suspend function (`applyUiAction` and whatever it calls) - both resolve
to a plain `String` before it reaches `UiData`/`UiEvent`, so the same pattern works unmodified for
Pattern B's native SwiftUI. See [ARCHITECTURE.md](ARCHITECTURE.md) for the full walkthrough. The
boilerplate script scaffolds the same shape (default locale only) for new features.

## iOS ↔ KMP Integration

- `KoinStarter.start()` — must run before anything touches Koin; called once from
  `Whole30JournalApp.init()`. Initializes `KoinIOS`, which (unlike Android's `GlobalContext`) keeps an
  explicit `KoinApplication` reference.
- `KoinResolver.get(_:)` — the one bridge from Swift into the Koin container (ObjC-class reflection
  under the hood). Deliberately independent of any feature module, so it never needs to know about
  specific ViewModel types.
- No per-feature factory wrapper — call `KoinResolver.get(SomeViewModel.self)` directly where the
  ViewModel is needed; see ARCHITECTURE.md.
- `ComposeViewController` (`UIViewControllerRepresentable`, `iosApp/iosApp/Core/`) wraps a Kotlin
  `ComposeUIViewController { ... }` factory for embedding shared Compose screens. Native SwiftUI
  screens instead observe `viewModel.state` / `viewModel.outputEvents` directly via SKIE's
  `for await` + `onEnum(of:)`.

## Key Technologies

Kotlin 2.4.0 (multiplatform) · Compose Multiplatform 1.11.1 (Material3 1.9.0) · Koin 4.2.2
(DI — shared + Android; iOS via `KoinIOS`/`KoinResolver`, no swift-dependencies) · SKIE 0.10.13
(Kotlin↔Swift interop) · kotlinx.coroutines 1.11.0 · androidx.lifecycle 2.11.0 · AGP 9.3.1.

See [gradle/libs.versions.toml](gradle/libs.versions.toml) for the always up-to-date version
catalog.

**Gradle gotcha:** `gradle.properties` sets `android.builtInKotlin=false` / `android.newDsl=false`
because AGP 9's built-in Kotlin support conflicts with the explicitly-applied
`kotlin.android`/`kotlin.multiplatform` plugins used across every module. Don't remove these
without confirming that conflict no longer applies.

## Naming Conventions

- Feature names: kebab-case at the directory level (e.g. `weather`), no dash in the package
  segment (`dev.whole30journal.feature.weather`).
- Packages: `dev.whole30journal.feature.<name>.<layer>` (`domain`/`data`/`presentation`),
  `dev.whole30journal.core.<module>` for shared core.
- Classes: PascalCase with descriptive suffixes — `<Feature>Repository`, `Get<X>UseCase`,
  `<Feature>ViewModel`, `<Feature>Contract`, `<Feature>{Domain,Data,Presentation}Module`.
- Inside a `presentation` module: `vm/` (Contract + ViewModel), `ui/` (commonMain composables,
  only present for shared-UI features), `di/`. iOS-only view-controller wrappers live under
  `iosMain/.../viewcontroller/`.

## Feature Development

Only run this when explicitly requested by the developer - don't scaffold a new feature just
because a task involves one:
```bash
./scripts/create_kmp_feature_boilerplate.sh <feature-name>   # lower-kebab-case
```
This generates a full `domain`/`data`/`presentation` module set (Contract, `StateFlowViewModel`,
fake in-memory repository, shared Compose screen, iOS `ViewController` wrapper, Koin DI modules —
see [ARCHITECTURE.md](ARCHITECTURE.md) for the shape) and wires it into `settings.gradle.kts`,
`shared/umbrella/build.gradle.kts` (exports + dependencies), and `AppModules.kt`. It does **not**
wire the new screen into `androidApp`'s navigation or `iosApp`'s UI, and it does not replace the
placeholder sample data in the generated `Fake<Feature>Repository` - both are deliberate manual
follow-ups (see the script's own end-of-run summary). After running it, `cd iosApp && xcodegen
generate` to pick up the new Swift-facing files if you add any under `iosApp/iosApp/`.

## Deliberate Simplifications

This repo intentionally omits things a production KMP app would have — don't add them unless
explicitly asked:

- No example feature checked in — don't scaffold one just to have it; use the script above when a
  task actually calls for a new feature.
- No Ktor — a generated feature's fake in-memory repository stands in for a real backend; keep
  domain/presentation depending only on the repository interface so swapping it in later doesn't
  touch other layers.
- No `build-logic` convention plugins — every module has a plain, explicit `build.gradle.kts`.
- No market/flavor variants, no MokoResources, no analytics/crash reporting, no Room.
- No automated tests.

## Working Conventions

- Follow the patterns in ARCHITECTURE.md rather than introducing new abstractions — this is a
  small, deliberately-readable reference project, not a place to demonstrate other approaches.
- Commit messages: short, imperative mood, no ticket prefix (e.g. `Rename BaseScopedViewModel to
  BaseViewModel`).
- Verify against actual file contents before assuming architecture.
