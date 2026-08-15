# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project Overview

Whole30 Journal — a Kotlin Multiplatform app for Android and iOS. The user configures a program
(start date + duration), logs one entry per day (four scored metrics, meals with photos,
achievements, notes), and reviews progress and trends on the home screen. Every screen is a shared
Compose Multiplatform screen; each app module owns navigation and DI bootstrap only.

Read [ARCHITECTURE.md](ARCHITECTURE.md) in full before changing anything structural — it is the
reference for the module layout, the MVI foundation, the data layer, and the iOS bridge.

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
Then run the `iosApp` scheme in Xcode — a Run Script build phase runs
`./gradlew :shared:umbrella:embedAndSignAppleFrameworkForXcode` and embeds `SharedKit.framework`.
`iosApp.xcodeproj` is generated from `project.yml` and is not checked into git; regenerate it after
adding or removing files under `iosApp/iosApp/`.

### Tests and static analysis
```bash
./gradlew jvmTest testDebugUnitTest   # what CI runs
./gradlew detekt                      # Kotlin static analysis
```
`jvmTest` is where the repository tests actually run — `shared/core/database` declares a `jvm()`
target solely so they execute fast on the host through SQLDelight's JDBC driver. `allTests` also
pulls in `iosSimulatorArm64Test`, which needs a macOS host to even compile, so CI does not run it.
SwiftLint runs as an Xcode Run Script build phase (config at `config/swiftlint/.swiftlint.yml`,
left on the default rule set); Detekt config is at `config/detekt/detekt.yml` (150-char lines,
`MagicNumber`/`LongMethod`/`LongParameterList` off, `ru.kode` Compose rules on).

`.github/workflows/pr.yml` runs Detekt and the tests on every PR against `master`.

## Core Modules

- `shared:core:ui-uistate` — the shared-VM foundation: MVI contract (`UiStateAware` /
  `UiActionAware`) and `StateFlowViewModel`.
- `shared:core:database` — SQLDelight `Whole30Database` (`ProgramEntity`, `DayEntryEntity`,
  `MetricEntity`, `MealEntity`, `AchievementEntity`), the `expect`/`actual`
  `DatabaseDriverFactory`, the shared `databaseDispatcher`, and `runCatchingCancellable`.
- `shared:core:design-system` — `DSTheme` + `DS*` components. Feature UI uses these, not raw
  `MaterialTheme` colors or hardcoded dimensions.
- `shared:core:utils` — `DateFormatter` (suspend, resource-backed) and `dateForDay`.
- `shared:feature:day-entry` — `domain` + `data` + `presentation`; the day logging/editing screen.
- `shared:feature:program` — `domain` + `data`; program configuration. No UI of its own.
- `shared:feature:home` / `settings` / `day-detail` — `presentation` only; each reuses another
  feature's `domain` module (day-entry + program, program, day-entry + program respectively).
  `day-detail` additionally depends on `day-entry`'s presentation module for the meal-photo
  resolver and `HeartIcon` — the one cross-presentation edge; don't add a second one.
- `shared:umbrella` — builds the `SharedKit` framework for iOS; hosts `KoinIOS` and `appModules`.
- `androidApp` — Compose app. `MainActivity` → `App()` → `HomeRoute()`, which owns the `NavHost`.
  Koin starts in `Whole30JournalApp.onCreate()`.
- `iosApp` — SwiftUI app. `Whole30JournalApp.init()` → `KoinStarter.start()`; `ContentView` →
  `HomeView`, which owns the `NavigationStack`. Bridge infrastructure lives in `iosApp/iosApp/Core/`.

If you see `shared/core/network`, `shared/feature/example`, or `shared/feature/diary` on disk, they
are stale, untracked `build/` leftovers from deleted modules — not source. Nothing in
`settings.gradle.kts` references them.

## Shared ViewModel Pattern (the core mechanic)

See [ARCHITECTURE.md](ARCHITECTURE.md) for the full pattern with code. In short:

- Each feature defines `object <Feature>Contract { UiData, UiAction, UiEvent, OutputEvent }`.
  `UiData` is `@Immutable` and holds only already-resolved, Compose-stable values.
- `StateFlowViewModel` is a plain class: a real `androidx.lifecycle.ViewModel` on Android, the same
  class instantiated as a Kotlin object owned by Swift on iOS — no `expect`/`actual` split needed.
- Feature VMs extend it and use `updateUiData {}` / `updateIsLoading()` / `updateUiEvents {}` /
  `emitOutputEvent()` — never touch the state flow directly.
- `UiEvent`s are transient and consumed once via `onUiEventConsumed` (e.g. a snackbar).
  `OutputEvent`s are one-shot side effects over a `SharedFlow`, never replayed — navigation goes
  here, and both platforms translate them into their own navigation.
- Within one `applyUiAction` call, don't read `currentUiData` back after that same call already
  wrote to it — the read can still see the pre-update value. Resolve it once into a local.
- `clearScope()` is terminal. Only a presenter that genuinely owns the VM's teardown may call it;
  calling it from a view's disappear callback permanently kills the screen's state.

## Data and Repository Conventions

- Repositories return `Result<T>` and never throw across a layer boundary. Wrap DB work in
  `runCatchingCancellable` so `CancellationException` propagates rather than becoming a failed
  `Result`.
- All database access goes through `withContext(databaseDispatcher)`. Do not introduce a second
  confinement scheme — the SQLite connection is shared.
- Observing multi-table state uses the merge/`drop(1)`/`onStart`/`conflate` + full-reread pattern in
  `DayEntryRepositoryImpl`, not `combine()` of per-table flows, which would expose torn state
  mid-transaction. Follow it when adding a new observed repository.
- Derived values (`Program.endDate`, `Program.currentDayNumber`) are computed on read, not stored.

## Localization

Strings live per-module in `src/commonMain/composeResources/values(-<lang>)/strings.xml` (Compose
Multiplatform Resources). The app ships the default locale plus `values-cs` — **add a Czech string
alongside every new default string**, in every presentation module and in `shared/core/utils`.
Rule of thumb: `stringResource()` from a composable, the suspend `getString()` from a plain suspend
function (`applyUiAction` and whatever it calls). Both resolve to a plain `String` before it reaches
`UiData`/`UiEvent`.

## iOS ↔ KMP Integration

- `KoinStarter.start()` — must run before anything touches Koin; called once from
  `Whole30JournalApp.init()`. Initializes `KoinIOS`, which keeps an explicit `KoinApplication`
  reference (unlike Android's `GlobalContext`).
- `KoinResolver.get(_:)` — the one bridge from Swift into the Koin container. No per-feature Swift
  factory wrapper; call it directly where a ViewModel is needed.
- `ComposeViewController` (`UIViewControllerRepresentable`, `iosApp/iosApp/Core/`) wraps a Kotlin
  `ComposeUIViewController { ... }` factory. `iosMain` view-controller factories need
  `@Suppress("FunctionName", "unused")` — only Swift calls them.
- `InteractivePopGesture` restores swipe-back, which UIKit disables because every destination hides
  the nav bar (shared Compose screens draw their own). Attach it to the `NavigationStack` root.
- Navigation pushes are de-duplicated on both platforms (a 1s debounce on Android, a same-route
  guard on iOS) because a fast double-tap dispatches two `UiAction`s before the first push lands.
- iOS `SharedKit` symbols only exist after a framework build; if Swift can't see a type, rebuild the
  umbrella and re-run `xcodegen generate`.

## Key Technologies

Kotlin 2.4.10 (multiplatform) · Compose Multiplatform 1.11.1 (Material3 1.9.0) · Koin 4.2.2
(DI — shared + Android; iOS via `KoinIOS`/`KoinResolver`) · SKIE 0.10.14 (Kotlin↔Swift interop) ·
SQLDelight 2.3.2 · Coil 3.5.0 (meal photos) · kotlinx.coroutines 1.11.0 · kotlinx-datetime 0.7.1 ·
kotlinx.serialization (type-safe Android routes) · androidx.lifecycle 2.11.0 ·
androidx.navigation-compose 2.9.8 · AGP 9.3.1 · minSdk 26 / targetSdk 34 / compileSdk 37 ·
iOS deployment target 16.0.

See [gradle/libs.versions.toml](gradle/libs.versions.toml) for the always up-to-date version
catalog.

**Gradle gotcha:** `gradle.properties` sets `android.builtInKotlin=false` / `android.newDsl=false`
because AGP 9's built-in Kotlin support conflicts with the explicitly-applied
`kotlin.android`/`kotlin.multiplatform` plugins used across every module. Don't remove these
without confirming that conflict no longer applies.

## Naming Conventions

- Feature names: kebab-case at the directory level (e.g. `day-entry`), no dash in the package
  segment (`dev.whole30journal.feature.dayentry`).
- Packages: `dev.whole30journal.feature.<name>.<layer>` (`domain`/`data`/`presentation`),
  `dev.whole30journal.core.<module>` for shared core, `dev.whole30journal.android.<screen>` for
  Android routes.
- Classes: PascalCase with descriptive suffixes — `<Feature>Repository`, `<Feature>RepositoryImpl`
  (internal), `Get<X>UseCase`/`Observe<X>UseCase`, `<Feature>ViewModel`, `<Feature>Contract`,
  `<Feature>{Domain,Data,Presentation}Module`.
- Inside a `presentation` module: `vm/` (Contract + ViewModel), `ui/` (commonMain composables, plus
  `ui/icons/` for `ImageVector` definitions), `di/`. iOS-only view-controller wrappers live in
  `iosMain/.../viewcontroller/`.
- Android app module: one `<screen>/<Screen>Route.kt` per destination, all destinations declared in
  `AppRoute.kt`. iOS: one `View/<Screen>View.swift` per destination.
- Design-system types are prefixed `DS`.

## Feature Scaffolding

Only run this when explicitly requested by the developer — don't scaffold a feature just because a
task involves one:
```bash
./scripts/create_kmp_feature_boilerplate.sh <feature-name>   # lower-kebab-case
```
It generates a `domain`/`data`/`presentation` module set (Contract, `StateFlowViewModel`, a fake
in-memory repository, a shared Compose screen, an iOS `ViewController` wrapper, Koin DI modules) and
wires it into `settings.gradle.kts`, `shared/umbrella/build.gradle.kts` (exports + dependencies) and
`AppModules.kt`.

**The script predates the current data layer, and its output does not match this codebase's
conventions.** Its comments still reference a `quote`/`counter` sample feature that no longer
exists, and after running it you must:

1. Replace the generated `Fake<Feature>Repository` with a SQLDelight-backed
   `<Feature>RepositoryImpl` (`Result<T>`, `databaseDispatcher`, `runCatchingCancellable`), and add
   the tables it needs to `shared/core/database`.
2. Add the Detekt block, `detektPlugins`, and `jvmTarget = "17"` to each generated
   `build.gradle.kts` — the script emits none, so the new modules would silently skip static
   analysis. Copy from an existing module.
3. Add `shared:core:design-system` and `shared:core:utils` to the presentation module's
   dependencies, since screens are built from `DS*` components.
4. Wire the screen in by hand: an Android `<Feature>Route.kt` plus an `AppRoute` entry and a
   `composable<…>` destination, and an iOS `View/<Feature>View.swift` plus a `HomeRoute` case.
5. Add `values-cs/strings.xml` next to the generated default strings.
6. `cd iosApp && xcodegen generate` to pick up new Swift files.

## Deliberate Simplifications

This repo intentionally omits things a larger production app would have — don't add them unless
explicitly asked:

- No networking layer at all — the app is fully local, SQLDelight is the only persistence.
- No `build-logic` convention plugins — every module has a plain, explicit `build.gradle.kts`,
  including a copy-pasted Detekt block.
- No market/flavor variants, no MokoResources, no analytics or crash reporting, no Room.
- No UI/instrumentation tests and no ViewModel tests. Test coverage today is the repository tests
  (`jvmAndIosTest` in the two `data` modules) and `ConfigureProgramUseCaseTest` (`commonTest`).
- No dependency-injection scoping beyond Koin singletons/factories.

## Working Conventions

- Follow the patterns in ARCHITECTURE.md rather than introducing new abstractions — keep the
  codebase readable and consistent over clever.
- **No explanatory comments in new or modified code.** Comments in this repo are reserved for
  non-obvious *why*s that would otherwise get "cleaned up" wrongly (the `-lsqlite3` linker flags,
  the `drop(1)`/`conflate` reasoning, `SWIFT_ENABLE_EXPLICIT_MODULES: NO`). Don't narrate what the
  code does.
- Commit messages: short, imperative mood, no ticket prefix (e.g. `Move home state and navigation
  out of App.kt into per-screen routes`).
- Branch names: `feature/…`, `fix/…`, `refactor/…`, `ci/…`. Work happens on a branch; PRs target
  `master`.
- Multiple agent sessions may share this working directory. Create a branch for your work rather
  than switching branches in place, and don't use `git worktree` here.
- Verify against actual file contents before assuming architecture.
- Run `./gradlew detekt` and the tests before proposing a change as done.
