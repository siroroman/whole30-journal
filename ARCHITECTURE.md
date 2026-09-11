# Architecture

Whole30 Journal is a Kotlin Multiplatform app for Android and iOS. A user configures a program
(start date + duration), logs one entry per day (four scored metrics, meals with photos,
achievements, notes), and reviews progress and trends on a home screen.

Everything a user sees is a **shared Compose Multiplatform screen** living in `commonMain`. Each
platform's app module owns only navigation, DI bootstrap, and platform plumbing. The shared
ViewModel foundation also supports fully-native UI per platform (see
[Pattern B](#pattern-b-native-ui-shared-viewmodel)), and iOS already uses that mechanic for
navigation, but no screen is written twice today.

## Module map

```
androidApp                       Compose app: MainActivity, App, AppRoute, per-screen *Route.kt
iosApp                           SwiftUI app: Whole30JournalApp, ContentView, View/, Core/

shared/core/ui-uistate           MVI contract + StateFlowViewModel (the shared-VM foundation)
shared/core/database             SQLDelight Whole30Database, driver factories, DB dispatcher
shared/core/design-system        DSTheme + DS* components (the app's whole visual language)
shared/core/utils                DateFormatter, dateForDay - shared date logic and its strings

shared/feature/day-entry         domain + data + presentation  (log/edit one day)
shared/feature/program           domain + data                 (program config, no UI of its own)
shared/feature/home              presentation                  (uses day-entry + program domain)
shared/feature/settings          presentation                  (uses program domain)
shared/feature/day-detail        presentation                  (uses day-entry domain)

shared/umbrella                  SharedKit XCFramework + KoinIOS + appModules
```

Layering inside a feature is `domain <- data <- presentation`: `domain` holds models, the
repository *interface*, and use cases; `data` implements the repository against
`shared/core/database`; `presentation` holds the MVI contract, the ViewModel, and the Compose
screen. A presentation module may depend on **another feature's domain module** — that is how
`home`, `settings`, and `day-detail` exist without domain/data layers of their own. A presentation
module never depends on another feature's `data`.

There is one cross-`presentation` dependency: `day-detail` depends on `day-entry`'s presentation
module for `rememberMealPhotoResolver` and the shared `HeartIcon`, because both screens render the
same meal photos. Prefer promoting anything more substantial into `core:design-system` or
`core:utils` over adding a second such edge.

## The shared ViewModel foundation

`shared/core/ui-uistate` is the piece both platforms build on.

`UiContract.kt` declares the MVI marker types every feature contract implements:

- `UiStateAware.UiData` — the screen's state.
- `UiStateAware.UiEvent` — transient, state-carried, consumed once via `onUiEventConsumed`
  (snackbars, one-off errors).
- `UiStateAware.OutputEvent` — one-shot side effects (navigation), delivered over a `SharedFlow`,
  never replayed.
- `UiStateAware.UiState<S, E>` — `isLoading` + `uiData` + `uiEvents`, the single object a screen
  renders from.
- `UiActionAware.UiAction` — everything the UI can do.

`vm/StateFlowViewModel.kt` is the base class every feature ViewModel extends. It is a real
`androidx.lifecycle.ViewModel`, and the *same class* is instantiated as a plain Kotlin object owned
by Swift on iOS — no `expect`/`actual` split, since `ViewModel` and `viewModelScope` are ordinary
KMP APIs on both platforms. It owns a `MutableStateFlow`, exposes it as `state`, and routes
`onUiAction` into the abstract `suspend applyUiAction` on `viewModelScope`.

Update state through its helpers, never by touching the flow:

| Helper | Use for |
| --- | --- |
| `updateUiData(isLoading) { copy(...) }` | the common case |
| `updateIsLoading(Boolean)` | loading flag alone |
| `updateUiEvents { it + SomeEvent }` | queueing a transient event |
| `emitOutputEvent(event)` | navigation / one-shot side effect |
| `currentUiData` / `currentUiState` | reading the latest value |

`clearScope()` cancels `viewModelScope`'s children and calls `onCleared()`. It exists for iOS,
where nothing owns the VM's lifecycle automatically. It is **terminal** — a cleared VM cannot be
reused, so only a presenter that is genuinely done with the VM may call it.

A contract looks like this (from `HomeContract`):

```kotlin
object HomeContract {

    @Immutable
    data class UiData(
        val needsSetup: Boolean = false,
        val currentDay: Int = 0,
        val days: List<DayCell> = emptyList(),
        // ...
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data class OnDayClick(val dayNumber: Int) : UiAction
        data object OnSettingsClick : UiAction
    }

    sealed interface UiEvent : UiStateAware.UiEvent

    sealed interface OutputEvent : UiStateAware.OutputEvent {
        data class NavigateToDayEntry(val dayNumber: Int) : OutputEvent
        data object NavigateToSettings : OutputEvent
    }
}
```

`UiData` is annotated `@Immutable` and holds only Compose-stable, already-resolved values —
strings are plain `String`s by the time they land here, never `StringResource`s.

## Pattern A: shared Compose Multiplatform UI

The pattern every screen in the app uses.

**1. The screen lives in `commonMain`**, e.g.
`shared/feature/home/presentation/.../ui/HomeScreen.kt`:

```kotlin
@Composable
fun HomeScreen(
    state: UiStateAware.UiState<HomeContract.UiData, HomeContract.UiEvent>,
    onUiAction: (HomeContract.UiAction) -> Unit,
    onUiEventConsume: (HomeContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Plain Compose Multiplatform on top of DSTheme - nothing Android- or iOS-specific.
}
```

The screen is stateless with respect to the VM: state in, actions out. Screens draw their own top
bar (neither platform's native navigation chrome is used).

**2. An `iosMain` wrapper turns it into a `UIViewController`** (same module,
`.../presentation/viewcontroller/HomeScreenViewController.kt`):

```kotlin
@Suppress("FunctionName", "unused")
fun HomeScreenViewController(viewModel: HomeViewModel): UIViewController =
    ComposeUIViewController { HomeRoot(viewModel = viewModel) }

@Composable
private fun HomeRoot(viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
```

The `@Suppress("unused")` is required: nothing in Kotlin calls this function — only Swift does,
through SKIE.

**3. On Android**, a `*Route.kt` composable resolves the VM with `koinViewModel()`, collects state
with `collectAsStateWithLifecycle()`, translates `outputEvents` into navigation, and calls the
shared screen.

**4. On iOS**, a SwiftUI `View` resolves the VM through `KoinResolver` and embeds the view
controller with the generic `ComposeViewController` wrapper (`iosApp/iosApp/Core/`):

```swift
struct DayEntryView: View {
    let dayNumber: Int

    @Environment(\.dismiss) private var dismiss
    private let viewModel: DayEntryViewModel = KoinResolver.get(DayEntryViewModel.self)

    var body: some View {
        ComposeViewController {
            DayEntryScreenViewController(viewModel: viewModel)
        }
        .ignoresSafeArea()
        .onAppear {
            viewModel.onUiAction(uiAction: DayEntryContractUiActionOnAppear(dayNumber: Int32(dayNumber)))
        }
        .task {
            for await event in viewModel.outputEvents {
                switch onEnum(of: event) {
                case .close: dismiss()
                }
            }
        }
    }
}
```

## Pattern B: native UI, shared ViewModel

No shared UI: Android gets a Jetpack Compose screen, iOS gets a SwiftUI screen, both driven by the
same `StateFlowViewModel` subclass. Nothing in the app uses this for a full screen today, but the
foundation supports it and iOS already relies on the mechanic — `HomeView` observes
`viewModel.state` directly to decide between the loading spinner, the setup screen, and the home
content, and every iOS view consumes `outputEvents` this way.

SKIE bridges `StateFlow`/`SharedFlow` to Swift `AsyncSequence`, so a plain `for await` works:

```swift
.task {
    for await state in viewModel.state {
        if isLoading != state.isLoading { isLoading = state.isLoading }
        if needsSetup != state.uiData.needsSetup { needsSetup = state.uiData.needsSetup }
    }
}
```

`onEnum(of:)` is what makes a Kotlin `sealed interface` switch exhaustively from Swift — without it
you would be testing each case with `is`/`as?`.

Because a Pattern B ViewModel resolves its strings the same way a Pattern A one does (suspend
`getString()` inside `applyUiAction`, see [Localizing strings](#localizing-strings)), native SwiftUI
observing `viewModel.state` never touches Compose Resources at all.

## Navigation

Navigation is **not** shared. Both platforms drive it off the same `OutputEvent`s.

### Android

`MainActivity` sets `App()`, which renders `HomeRoute()`. `HomeRoute` is the whole navigation host:

- It resolves `HomeViewModel` and gates on state — while `isLoading`, nothing renders; if
  `uiData.needsSetup`, `SettingsRoute` renders standalone (no back stack, first-run setup);
  otherwise the `NavHost` renders.
- Destinations are the type-safe `@Serializable` variants of `AppRoute` (`Home`, `Settings`,
  `DayDetail(dayNumber)`, `DayEntry(dayNumber)`), with slide transitions in both directions.
- `HomeContract.OutputEvent`s are collected in a `LaunchedEffect` and turned into `navigate` calls
  through `rememberDebouncedNavigate`, which drops a repeat navigation within 1s — a fast
  double-tap dispatches two `UiAction`s before the first navigation lands.

`SettingsRoute` provides its own `ViewModelStoreOwner` via `LocalViewModelStoreOwner` and clears it
`onDispose`, so the settings VM is genuinely recreated per visit instead of surviving in the
activity's store (it is shown both as the first-run gate and as a normal destination).

### iOS

`Whole30JournalApp.init()` calls `KoinStarter.start()`; `ContentView` renders `HomeView`.
`HomeView` mirrors the Android gate (spinner / `SettingsView` / home content) by observing
`viewModel.state`, and owns a `NavigationStack` driven by a private `HomeRoute` enum path.

- `outputEvents` push onto `path`; `push(_:)` refuses to push the route already on top, the iOS
  counterpart of Android's navigation debounce.
- Every destination hides the navigation bar, because the shared Compose screens draw their own.
  That makes UIKit disable the interactive pop gesture, so `InteractivePopGesture`
  (`.interactivePopGestureEnabled()` on the stack root) reinstalls a delegate that re-enables
  swipe-back and allows it to recognize simultaneously with Compose's own recognizers.

## Data layer

`shared/core/database` owns one SQLDelight database, `Whole30Database`, generated into
`dev.whole30journal.core.database` from `src/commonMain/sqldelight/`:

| Table | Holds |
| --- | --- |
| `ProgramEntity` | single row: `startDate`, `durationDays` |
| `DayEntryEntity` | one row per day: `dayNumber` (PK), `date`, `notes`, `isComplete` |
| `MetricEntity` | `(dayNumber, title)` PK: score `value`, `maxValue`, `iconName`, `note` |
| `MealEntity` | per-day meals: `description`, `photoToken`, `lovedIt`, `sortOrder` |
| `AchievementEntity` | per-day achievements: `text`, `sortOrder` |

`Program` is not stored whole: `endDate` and `currentDayNumber` are derived from `startDate` +
`durationDays` and today's date when the row is read. Configuring a program pre-creates a
`DayEntryEntity` row per day (`insertIfAbsent`), rewrites every day's `date`, and deletes rows past
the new duration — all in one transaction.

Three conventions matter here:

- **`DatabaseDriverFactory` is `expect`/`actual`**: `AndroidSqliteDriver` (needs a `Context`, hence
  the separate `androidDatabaseModule`), `NativeSqliteDriver` on iOS, and a JDBC driver on `jvm()`.
  The JVM target exists **only** so repository tests run fast on the host — it ships in no app.
- **All DB access is confined to `databaseDispatcher`**, a single shared dispatcher in
  `shared/core/database`. The SQLite connection is not safe for concurrent multi-threaded use, and
  per-repository confinement would not mutually exclude a second repository on the same connection.
- **Repositories return `Result<T>`**, produced by `runCatchingCancellable` so a
  `CancellationException` propagates instead of being swallowed into a failed `Result`. Domain,
  presentation, and the UI all carry that `Result`; nothing throws across a layer boundary.

Observation is deliberately not a `combine()` of per-table flows. A save touches four tables in one
transaction, and four independently-invalidated flows would let a collector observe a torn
intermediate state. Instead the repository merges the four query flows into a single "something
changed" signal, `drop(1)`s each source's synthetic initial emission, adds exactly one back with
`onStart`, `conflate()`s the burst a commit produces, and re-reads everything fresh per tick:

```kotlin
val invalidations = merge(
    database.dayEntryQueries.selectByDayNumber(dayNumber).asFlow().map { }.drop(1),
    database.metricQueries.selectByDayNumber(dayNumber).asFlow().map { }.drop(1),
    // meals, achievements...
).onStart { emit(Unit) }

return invalidations
    .conflate()
    .map { withContext(dbDispatcher) { loadDayEntry(dayNumber) } }
    .distinctUntilChanged()
    .map { Result.success(it) }
    .catch { e -> if (e is CancellationException) throw e else emit(Result.failure(e)) }
```

## Design system

`shared/core/design-system` is the app's visual language; feature screens should not reach for raw
`MaterialTheme` colors or hardcoded values.

`DSTheme { }` provides `LocalDSColor` and `LocalDSTypography` over a `MaterialTheme`, picking
`dsDarkColors()` / `dsLightColors()` from `isSystemInDarkTheme()`. Read them through the `DSTheme`
object: `DSTheme.colors.accent`, `DSTheme.typography.…`. `DSColor` is a flat semantic palette
(`bg`, `surface`, `surface2`, `divider`, `text`/`textSecondary`/`textTertiary`, `accent`,
`scoreLow`/`scoreMid`/`scoreHigh`, per-metric icon colors, …) plus the `DSColor.scoreColor(score)`
helper that maps a 0–10 score onto low/mid/high. Spacing, radii, shapes, and fonts are
`DSSpacing`, `DSRadius`, `DSShapes`, `dsFontFamily()`.

Components: `DSButton`, `DSCard`, `DSProgressBar`, `DSProgressRing`, `DSScoreDots`, `DSTag` (+
`DSTagTone`), `DSTextField`.

`DSTextField` takes hoisted `value: String` / `onValueChange`, but keeps a local `TextFieldValue`
internally and re-syncs the caret to the end when the hoisted value diverges. This is load-bearing:
a plain `TextField(value: String)` fed from VM state resets the cursor to position 0 on every
keystroke that round-trips through the ViewModel.

## Meal photos

Photo capture is the app's one real `expect`/`actual` UI seam, in
`day-entry/presentation/.../photo/`:

```kotlin
interface MealPhotoPicker {
    fun launchCamera()
    fun launchLibrary()
}

@Composable expect fun rememberMealPhotoPicker(onPhotoSave: (String) -> Unit): MealPhotoPicker
@Composable expect fun rememberMealPhotoResolver(): (String) -> String
```

What crosses into shared code is a **filename token**, never a platform path or URI — that token is
what `MealEntity.photoToken` stores. `rememberMealPhotoResolver()` turns it back into an absolute
path at render time, which Coil loads.

- **Android**: `rememberLauncherForActivityResult` with `TakePicture` / `PickVisualMedia`, files
  written to `filesDir/meal-photos`, camera output handed out via `FileProvider` (authority
  `${packageName}.fileprovider`, paths in `androidApp/src/main/res/xml/file_paths.xml`).
- **iOS**: `UIImagePickerController` for the camera and `PHPickerViewController` for the library,
  presented from `LocalUIViewController.current`, JPEG written into the documents directory.

Note for iOS: presenting a fullscreen camera controller over a shared Compose screen fires
SwiftUI's `.onDisappear` on the presenting view. Do not tear the ViewModel down there — cancelling
its scope while the picker is up permanently kills the screen's state.

## Localizing strings

Strings live per-module under
`src/commonMain/composeResources/values/strings.xml` (default) with `values-<lang>/strings.xml`
siblings — the app currently ships default plus `values-cs` in every presentation module and in
`shared/core/utils`. Each module generates its own `Res` object under an explicit package, set in
its `build.gradle.kts`:

```kotlin
compose.resources {
    packageOfResClass = "dev.whole30journal.feature.<name>.presentation.generated.resources"
}
```

Which resolution function to use depends on whether the call site is composable:

- **Composable call sites** (`ui/*Screen.kt` and friends) use `stringResource(Res.string.x)`.
- **Plain suspend call sites** (`applyUiAction` and anything it calls, e.g. a VM's private
  `load…()`) use the suspend `getString(Res.string.x)`, since `stringResource` is `@Composable`-only.

`DateFormatter` in `shared/core/utils` is the reason weekday and "Today" strings are suspend
functions rather than composables: it is called from ViewModels, and every label it produces lands
in `UiData` as a plain `String`.

## The iOS <-> Koin bridge

Two files in `iosApp/iosApp/Core/`, both independent of any feature module:

- `KoinStarter.start()` must run before anything touches Koin — called once from
  `Whole30JournalApp.init()`. It initializes `KoinIOS` (`shared/umbrella`), which, unlike Android's
  `GlobalContext`, keeps an explicit `KoinApplication` reference.
- `KoinResolver.get(SomeViewModel.self)` is the only bridge from Swift into the container
  (ObjC-class reflection under the hood). There is no per-feature Swift factory — call it directly
  where a ViewModel is needed:

```swift
private let viewModel: SettingsViewModel = KoinResolver.get(SettingsViewModel.self)
```

On Android, `Whole30JournalApp.onCreate()` runs `startKoin { androidContext(this); modules(appModules + androidDatabaseModule) }`;
iOS's `KoinIOS` does the equivalent with `iosDatabaseModule`. `appModules`
(`shared/umbrella/.../di/AppModules.kt`) is the single list both platforms start from, and each
feature module contributes its own `*DomainModule` / `*DataModule` / `*PresentationModule`.

## The SharedKit framework

`shared/umbrella` builds the static `SharedKit` framework (`iosArm64`, `iosSimulatorArm64`) with
SKIE's `swiftBundling` enabled. Only project modules can be `export(...)`ed into the framework's
Swift namespace — third-party deps like Koin and coroutines stay linked-in but un-exported. A
module Swift needs to *name* (a ViewModel, a contract type, a domain model) must be both `export`ed
in the framework block and declared `api(...)` in `commonMain`; a purely internal module (the `data`
modules, `core:database`) is `implementation(...)` only.

Two build settings in `iosApp/project.yml` follow from this and should not be "cleaned up":

- `OTHER_LDFLAGS: -lsqlite3` — SQLDelight's native driver arrives transitively through
  `core:database`, and the app linking `SharedKit.framework` must supply the system sqlite3 symbols
  itself. The same reason drives the `linkerOpts` on the `data` modules' test binaries.
- `SWIFT_ENABLE_EXPLICIT_MODULES: NO` — SKIE's `swiftBundling` embeds a binary `.swiftmodule` with
  no `.swiftinterface`; Xcode's explicit-module scanner sees only the Clang/ObjC side and silently
  drops the SKIE-generated free functions such as `HomeScreenViewController(viewModel:)`.

## Adding a new feature

```bash
./scripts/create_kmp_feature_boilerplate.sh <feature-name>
```

scaffolds a `domain`/`data`/`presentation` module set and wires it into `settings.gradle.kts`,
`shared/umbrella/build.gradle.kts`, and `AppModules.kt`. Read
[AGENTS.md](AGENTS.md#feature-scaffolding) before using it — the script predates the current data
layer and its generated code needs edits to match the conventions above.
