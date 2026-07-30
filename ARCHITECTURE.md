# Architecture

This project demonstrates two ways to share code between Android and iOS on top of the same
Kotlin Multiplatform ViewModel foundation. `example` (`shared/feature/example`) is the one example
feature checked in, showing Pattern A end-to-end; Pattern B has no checked-in example, so this doc
is still the full reference for both. Use
[`scripts/create_kmp_feature_boilerplate.sh`](scripts/create_kmp_feature_boilerplate.sh) to
scaffold a new feature shaped like the examples below (see [AGENTS.md](AGENTS.md)).

The illustrative code below (`Weather`, `Counter`) is representative, not code that exists in the
repo - it shows the shape a real feature takes.

## Pattern A: shared Compose Multiplatform UI

The screen itself is written once, in `commonMain`, and rendered natively on both platforms - no
platform-specific UI code at all.

**1. The screen lives in `commonMain`** (`shared/feature/weather/presentation/.../ui/WeatherScreen.kt`):
```kotlin
@Composable
fun WeatherScreen(
    state: UiStateAware.UiState<WeatherContract.UiData, WeatherContract.UiEvent>,
    onUiAction: (WeatherContract.UiAction) -> Unit,
    onUiEventConsume: (WeatherContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Plain Compose Multiplatform: Scaffold, Text, Button, MaterialTheme... nothing Android- or
    // iOS-specific in this file.
}
```

**2. An `iosMain` wrapper turns it into a `UIViewController`** (same module,
`.../presentation/viewcontroller/WeatherScreenViewController.kt`):
```kotlin
fun WeatherScreenViewController(viewModel: WeatherViewModel): UIViewController =
    ComposeUIViewController {
        val state by viewModel.state.collectAsStateWithLifecycle()
        WeatherScreen(
            state = state,
            onUiAction = { viewModel.onUiAction(it) },
            onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        )
    }
```

**3. On Android**, call the composable directly from Jetpack Compose - `koinViewModel()` for the
VM, `collectAsStateWithLifecycle()` for the state, done.

**4. On iOS**, wrap the `UIViewController` factory in a `UIViewControllerRepresentable` so SwiftUI
can embed it. That wrapper is `iosApp/iosApp/Core/ComposeViewController.swift` and it's generic -
every shared-UI feature reuses the same one:
```swift
struct WeatherView: View {
    let viewModel: WeatherViewModel

    var body: some View {
        ComposeViewController {
            WeatherScreenViewController(viewModel: viewModel)
        }
        .onAppear {
            viewModel.onUiAction(uiAction: WeatherContractUiActionOnAppear())
        }
    }
}
```
SwiftUI only owns navigation chrome and lifecycle here; the screen's actual UI is Kotlin.

## Pattern B: native UI, shared ViewModel

No shared UI at all - Android gets a native Jetpack Compose screen, iOS gets a native SwiftUI
screen, and both are driven by *the exact same* `ViewModel` instance type, running unmodified on
both platforms (see "The shared ViewModel foundation" below for how one class manages that).

**Android** side is unremarkable Compose:
```kotlin
val viewModel: CounterViewModel = koinViewModel()
val state by viewModel.state.collectAsStateWithLifecycle()
Button(onClick = { viewModel.onUiAction(CounterContract.UiAction.OnIncrementClick) }) { Text("+") }
```

**iOS** has no Kotlin UI to wrap, so it observes the ViewModel's Kotlin `Flow`s directly. SKIE
bridges `StateFlow`/`SharedFlow` to Swift's `AsyncSequence`, so a plain `for await` loop works:
```swift
struct CounterView: View {
    let viewModel: CounterViewModel
    @State private var count = 0

    var body: some View {
        VStack {
            Text("\(count)")
            Button("+") { viewModel.onUiAction(uiAction: CounterContractUiActionOnIncrementClick()) }
        }
        .task {
            for await state in viewModel.state {          // StateFlow -> AsyncSequence
                count = Int(state.uiData.count)
            }
        }
        .task {
            for await event in viewModel.outputEvents {    // SharedFlow -> AsyncSequence
                switch onEnum(of: event) {                 // SKIE: exhaustive switch over a
                case let .milestoneReached(data):          // Kotlin sealed interface, from Swift
                    print("Milestone: \(data.value)")
                }
            }
        }
    }
}
```
`onEnum(of:)` is what makes a Kotlin `sealed interface`/`sealed class` switch exhaustively in
Swift - without it you'd be stuck testing each case with `is`/`as?`.

## The shared ViewModel foundation

Both patterns sit on top of `shared/core/ui-uistate`, which is unaffected by any of this - see the
source directly:
- `UiContract.kt` - the MVI marker types every feature contract implements: `UiStateAware.{UiData,
  UiEvent, OutputEvent, UiState}` and `UiActionAware.UiAction`.
- `vm/BaseViewModel.kt` - `expect/actual`: a real `androidx.lifecycle.ViewModel` on Android, a
  plain Kotlin object owned by Swift on iOS. Both `actual`s turn a `@Composable getState()` into a
  `StateFlow` via **Molecule** - this is what lets one VM subclass run unmodified on both
  platforms without a hand-rolled reducer.
- `vm/ComposeStateViewModel.kt` - wires action dispatch (`onUiAction` -> `applyUiAction`) and the
  one-shot `outputEvents: SharedFlow<O>`.
- `vm/StateFlowViewModel.kt` - the convenience base almost every feature VM extends: holds a
  `MutableStateFlow`, exposes `updateUiData {}` / `updateIsLoading()` / `updateUiEvents {}` /
  `emitOutputEvent()` helpers instead of making you touch the flow directly.

## Localizing strings

Any feature's `presentation` module can declare its own string resources via [Compose Multiplatform
Resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources.html): strings
live in `<feature>/presentation/src/commonMain/composeResources/values/strings.xml` (default
locale), with `values-<lang>/strings.xml` siblings for each additional locale (e.g.
`values-es/strings.xml`). Each module generates its own `Res` object under an explicit package,
rather than relying on an undocumented default - `example`'s presentation module sets this in
`build.gradle.kts`:
```kotlin
compose.resources {
    packageOfResClass = "dev.whole30journal.feature.example.presentation.generated.resources"
}
```

**Which resolution function to use depends on whether the call site is composable** - this ties
directly back to the `getState()`/`applyUiAction` split above:
- **Composable call sites** (`ui/*Screen.kt`, or a custom `getState()` override) use
  `stringResource(Res.string.x)` - see the "Get Another Fact" button in `ExampleScreen.kt`.
- **Plain suspend call sites** (`applyUiAction` and anything it calls, e.g. a ViewModel's private
  `load...()` helper) use the suspend `getString(Res.string.x)` instead, since `stringResource` is
  `@Composable`-only - see `ExampleViewModel.kt`'s `loadCatFact()`, which resolves the error message
  to a plain `String` before constructing `UiEvent.ShowError`.

This is also the answer for Pattern B: a Pattern B ViewModel resolves strings exactly the same way
(suspend `getString()` inside `applyUiAction`), so native SwiftUI observing `viewModel.state`
directly (see the `CounterView` example above) never touches `Res`/Compose Resources at all - by
the time a string reaches `UiData`/`UiEvent`/`OutputEvent`, it's already a plain `String`.

`scripts/create_kmp_feature_boilerplate.sh` scaffolds this same shape automatically for new
features (default locale only); add further `values-<lang>/strings.xml` translations by hand as
needed.

## The iOS <-> Koin bridge

`iosApp/iosApp/KoinStarter.swift` and `KoinResolver.swift`, untouched by feature (de)scaffolding:
- `KoinStarter.start()` must run before anything touches Koin (called once from
  `Whole30JournalApp.init()`). It initializes `KoinIOS`, which - unlike Android's `GlobalContext` -
  keeps an explicit `KoinApplication` reference.
- `KoinResolver.get(SomeViewModel.self)` is the one bridge from Swift into the Koin container
  (ObjC-class reflection under the hood). It's deliberately independent of any feature module, so
  it never needs to know about specific ViewModel types.

Call `KoinResolver.get(SomeViewModel.self)` directly at the point a ViewModel is needed - no
per-feature factory wrapper, `KoinResolver` is already the whole indirection. See `ContentView.swift`:
```swift
struct ContentView: View {
    @State private var exampleViewModel = KoinResolver.get(ExampleViewModel.self)

    var body: some View {
        ExampleView(viewModel: exampleViewModel)
    }
}
```

## Adding a new feature

```bash
./scripts/create_kmp_feature_boilerplate.sh <feature-name>
```
generates a `domain`/`data`/`presentation` module set shaped like Pattern A above and wires it into
`settings.gradle.kts`, `shared/umbrella`, and `AppModules.kt`. Wiring the resulting screen or
ViewModel into `androidApp`'s and `iosApp`'s UI - a route/tab, a SwiftUI view calling
`KoinResolver.get(...)` - is a manual follow-up; see [AGENTS.md](AGENTS.md) for details and boundaries.
