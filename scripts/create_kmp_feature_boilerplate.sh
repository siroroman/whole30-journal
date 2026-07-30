#!/bin/bash

# Creates a complete Kotlin Multiplatform (KMP) feature with boilerplate code, following the same
# domain -> data -> presentation Clean Architecture split as the existing `quote` feature.
#
# Usage:
#   ./scripts/create_kmp_feature_boilerplate.sh <feature-name>
#
# Arguments:
#   1. feature-name - kebab-case name for the new feature (e.g. "fact", "user-profile")
#
# This script creates:
# - shared/feature/<name>/{domain,data,presentation} modules with build.gradle.kts
# - A Contract + ViewModel (StateFlowViewModel), a fake in-memory repository, a shared
#   Compose Multiplatform screen, an iOS ViewController wrapper, and Koin DI modules
# - Wiring into settings.gradle.kts, shared/umbrella (exports + deps), and AppModules.kt
#
# Only run this when explicitly requested (see AGENTS.md). It does NOT wire the new screen into
# androidApp's navigation or iosApp's tab bar - that's a deliberate manual follow-up step, same as
# how `quote`/`counter` are wired up by hand in App.kt / ContentView.swift.

set -euo pipefail

FEATURE_NAME="$1"

if [ -z "$FEATURE_NAME" ]; then
    echo "Error: feature-name is required."
    echo "Usage: $0 <feature-name>"
    echo "Example: $0 fact"
    exit 1
fi

if ! [[ "$FEATURE_NAME" =~ ^[a-z][a-z0-9]*(-[a-z0-9]+)*$ ]]; then
    echo "Error: feature-name must be lower-kebab-case (e.g. 'fact', 'user-profile')."
    exit 1
fi

if [ -d "shared/feature/$FEATURE_NAME" ]; then
    echo "Error: shared/feature/$FEATURE_NAME already exists."
    exit 1
fi

echo "Creating KMP feature boilerplate for: $FEATURE_NAME"

# Package segment: dash-stripped, all-lowercase (Kotlin package convention - see dev.whole30journal.feature.quote)
CLEAN_FEATURE_NAME=$(echo "$FEATURE_NAME" | tr -d '-')
# Gradle typesafe-project-accessor segment: kebab -> camelCase (e.g. "ui-uistate" -> "uiUistate")
PROJECT_ACCESSOR_NAME=$(echo "$FEATURE_NAME" | perl -pe 's/-(.)/uc($1)/ge')
# Class-name segment: PascalCase (e.g. "user-profile" -> "UserProfile")
CAMEL_CASE_FEATURE_NAME=$(echo "$PROJECT_ACCESSOR_NAME" | awk '{print toupper(substr($0,1,1)) substr($0,2)}')

DOMAIN_DIR="shared/feature/$FEATURE_NAME/domain/src/commonMain/kotlin/dev/whole30journal/feature/$CLEAN_FEATURE_NAME/domain"
DATA_DIR="shared/feature/$FEATURE_NAME/data/src/commonMain/kotlin/dev/whole30journal/feature/$CLEAN_FEATURE_NAME/data"
PRESENTATION_DIR="shared/feature/$FEATURE_NAME/presentation/src/commonMain/kotlin/dev/whole30journal/feature/$CLEAN_FEATURE_NAME/presentation"
IOS_PRESENTATION_DIR="shared/feature/$FEATURE_NAME/presentation/src/iosMain/kotlin/dev/whole30journal/feature/$CLEAN_FEATURE_NAME/presentation/viewcontroller"
COMPOSE_RESOURCES_DIR="shared/feature/$FEATURE_NAME/presentation/src/commonMain/composeResources/values"

echo "Creating module directories..."
mkdir -p "$DOMAIN_DIR/di"
mkdir -p "$DATA_DIR/di"
mkdir -p "$PRESENTATION_DIR/vm"
mkdir -p "$PRESENTATION_DIR/ui"
mkdir -p "$PRESENTATION_DIR/di"
mkdir -p "$IOS_PRESENTATION_DIR"
mkdir -p "$COMPOSE_RESOURCES_DIR"

echo "Generating build.gradle.kts files..."

cat > "shared/feature/$FEATURE_NAME/domain/build.gradle.kts" <<EOL
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }
}
EOL

cat > "shared/feature/$FEATURE_NAME/data/build.gradle.kts" <<EOL
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared.feature.$PROJECT_ACCESSOR_NAME.domain)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "dev.whole30journal.feature.$CLEAN_FEATURE_NAME.data"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }
}
EOL

cat > "shared/feature/$FEATURE_NAME/presentation/build.gradle.kts" <<EOL
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.shared.core.uiUistate)
            implementation(projects.shared.feature.$PROJECT_ACCESSOR_NAME.domain)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
        }
        iosMain.dependencies {
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}

compose.resources {
    packageOfResClass = "dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.generated.resources"
}

android {
    namespace = "dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation"
    compileSdk = libs.versions.sdk.compile.get().toInt()

    defaultConfig {
        minSdk = libs.versions.sdk.min.get().toInt()
    }
}
EOL

echo "Generating domain layer..."

cat > "$DOMAIN_DIR/$CAMEL_CASE_FEATURE_NAME.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain

data class $CAMEL_CASE_FEATURE_NAME(
    val text: String
)
EOL

cat > "$DOMAIN_DIR/${CAMEL_CASE_FEATURE_NAME}Repository.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain

interface ${CAMEL_CASE_FEATURE_NAME}Repository {
    suspend fun getRandom(): Result<$CAMEL_CASE_FEATURE_NAME>
}
EOL

cat > "$DOMAIN_DIR/GetRandom${CAMEL_CASE_FEATURE_NAME}UseCase.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain

class GetRandom${CAMEL_CASE_FEATURE_NAME}UseCase(
    private val repository: ${CAMEL_CASE_FEATURE_NAME}Repository
) {
    suspend operator fun invoke(): Result<$CAMEL_CASE_FEATURE_NAME> = repository.getRandom()
}
EOL

cat > "$DOMAIN_DIR/di/${CAMEL_CASE_FEATURE_NAME}DomainModule.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.di

import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.GetRandom${CAMEL_CASE_FEATURE_NAME}UseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val ${CLEAN_FEATURE_NAME}DomainModule = module {
    factoryOf(::GetRandom${CAMEL_CASE_FEATURE_NAME}UseCase)
}
EOL

echo "Generating data layer..."

cat > "$DATA_DIR/Fake${CAMEL_CASE_FEATURE_NAME}Repository.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.data

import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.$CAMEL_CASE_FEATURE_NAME
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.${CAMEL_CASE_FEATURE_NAME}Repository
import kotlinx.coroutines.delay

/**
 * Placeholder in-memory repository generated by the feature boilerplate script - replace with a
 * real implementation (e.g. Ktor-backed) when ready.
 */
internal class Fake${CAMEL_CASE_FEATURE_NAME}Repository : ${CAMEL_CASE_FEATURE_NAME}Repository {

    override suspend fun getRandom(): Result<$CAMEL_CASE_FEATURE_NAME> {
        delay(FAKE_NETWORK_DELAY_MS)
        return Result.success(${CLEAN_FEATURE_NAME}s.random())
    }

    private companion object {
        const val FAKE_NETWORK_DELAY_MS = 600L

        // TODO: replace with real sample data
        val ${CLEAN_FEATURE_NAME}s = listOf(
            $CAMEL_CASE_FEATURE_NAME("Sample $CAMEL_CASE_FEATURE_NAME 1"),
            $CAMEL_CASE_FEATURE_NAME("Sample $CAMEL_CASE_FEATURE_NAME 2"),
            $CAMEL_CASE_FEATURE_NAME("Sample $CAMEL_CASE_FEATURE_NAME 3"),
        )
    }
}
EOL

cat > "$DATA_DIR/di/${CAMEL_CASE_FEATURE_NAME}DataModule.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.data.di

import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.data.Fake${CAMEL_CASE_FEATURE_NAME}Repository
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.${CAMEL_CASE_FEATURE_NAME}Repository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val ${CLEAN_FEATURE_NAME}DataModule = module {
    singleOf(::Fake${CAMEL_CASE_FEATURE_NAME}Repository) { bind<${CAMEL_CASE_FEATURE_NAME}Repository>() }
}
EOL

echo "Generating presentation layer..."

cat > "$PRESENTATION_DIR/vm/${CAMEL_CASE_FEATURE_NAME}Contract.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.vm

import androidx.compose.runtime.Immutable
import dev.whole30journal.core.uistate.UiActionAware
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.$CAMEL_CASE_FEATURE_NAME

object ${CAMEL_CASE_FEATURE_NAME}Contract {

    @Immutable
    data class UiData(
        val $CLEAN_FEATURE_NAME: $CAMEL_CASE_FEATURE_NAME? = null
    ) : UiStateAware.UiData

    sealed interface UiAction : UiActionAware.UiAction {
        data object OnAppear : UiAction
        data object OnRefreshClick : UiAction
    }

    sealed interface UiEvent : UiStateAware.UiEvent {
        data class ShowError(val message: String) : UiEvent
    }

    sealed interface OutputEvent : UiStateAware.OutputEvent
}
EOL

cat > "$PRESENTATION_DIR/vm/${CAMEL_CASE_FEATURE_NAME}ViewModel.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.vm

import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.core.uistate.vm.StateFlowViewModel
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.GetRandom${CAMEL_CASE_FEATURE_NAME}UseCase
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.generated.resources.Res
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.generated.resources.${CLEAN_FEATURE_NAME}_error_load_failed
import org.jetbrains.compose.resources.getString

class ${CAMEL_CASE_FEATURE_NAME}ViewModel(
    private val getRandom$CAMEL_CASE_FEATURE_NAME: GetRandom${CAMEL_CASE_FEATURE_NAME}UseCase
) : StateFlowViewModel<${CAMEL_CASE_FEATURE_NAME}Contract.UiData, ${CAMEL_CASE_FEATURE_NAME}Contract.UiAction, ${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent, ${CAMEL_CASE_FEATURE_NAME}Contract.OutputEvent>(
    initialState = UiStateAware.UiState(uiData = ${CAMEL_CASE_FEATURE_NAME}Contract.UiData())
) {

    override suspend fun applyUiAction(uiAction: ${CAMEL_CASE_FEATURE_NAME}Contract.UiAction) {
        when (uiAction) {
            is ${CAMEL_CASE_FEATURE_NAME}Contract.UiAction.OnAppear -> load$CAMEL_CASE_FEATURE_NAME()
            is ${CAMEL_CASE_FEATURE_NAME}Contract.UiAction.OnRefreshClick -> load$CAMEL_CASE_FEATURE_NAME()
        }
    }

    private suspend fun load$CAMEL_CASE_FEATURE_NAME() {
        updateIsLoading(true)

        getRandom$CAMEL_CASE_FEATURE_NAME().fold(
            onSuccess = { $CLEAN_FEATURE_NAME ->
                updateUiData(isLoading = false) { copy($CLEAN_FEATURE_NAME = $CLEAN_FEATURE_NAME) }
            },
            onFailure = {
                val message = getString(Res.string.${CLEAN_FEATURE_NAME}_error_load_failed)
                updateUiEvents(isLoading = false) { it + ${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent.ShowError(message) }
            }
        )
    }
}
EOL

cat > "$PRESENTATION_DIR/ui/${CAMEL_CASE_FEATURE_NAME}Screen.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.whole30journal.core.uistate.UiStateAware
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.generated.resources.Res
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.generated.resources.${CLEAN_FEATURE_NAME}_button_new
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.vm.${CAMEL_CASE_FEATURE_NAME}Contract
import org.jetbrains.compose.resources.stringResource

/**
 * Written once, rendered natively on both platforms: embedded directly in Android's Compose tree,
 * and wrapped in a UIViewController for iOS (see ${CAMEL_CASE_FEATURE_NAME}ScreenViewController.kt in iosMain).
 */
@Composable
fun ${CAMEL_CASE_FEATURE_NAME}Screen(
    state: UiStateAware.UiState<${CAMEL_CASE_FEATURE_NAME}Contract.UiData, ${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent>,
    onUiAction: (${CAMEL_CASE_FEATURE_NAME}Contract.UiAction) -> Unit,
    onUiEventConsume: (${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding ->
        ${CAMEL_CASE_FEATURE_NAME}Content(
            uiData = state.uiData,
            isLoading = state.isLoading,
            onRefreshClick = { onUiAction(${CAMEL_CASE_FEATURE_NAME}Contract.UiAction.OnRefreshClick) },
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        )

        EventHandler(
            events = state.uiEvents,
            snackbarHostState = snackbarHostState,
            onConsume = onUiEventConsume
        )
    }
}

@Composable
private fun ${CAMEL_CASE_FEATURE_NAME}Content(
    uiData: ${CAMEL_CASE_FEATURE_NAME}Contract.UiData,
    isLoading: Boolean,
    onRefreshClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(contentPadding).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            uiData.$CLEAN_FEATURE_NAME?.let { $CLEAN_FEATURE_NAME ->
                Text(
                    text = $CLEAN_FEATURE_NAME.text,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Button(
            onClick = onRefreshClick,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        ) {
            Text(stringResource(Res.string.${CLEAN_FEATURE_NAME}_button_new))
        }
    }
}

@Composable
private fun EventHandler(
    events: List<${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent>,
    snackbarHostState: SnackbarHostState,
    onConsume: (${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent) -> Unit,
) {
    events.forEach { event ->
        when (event) {
            is ${CAMEL_CASE_FEATURE_NAME}Contract.UiEvent.ShowError -> {
                LaunchedEffect(event) {
                    snackbarHostState.showSnackbar(event.message)
                    onConsume(event)
                }
            }
        }
    }
}
EOL

cat > "$COMPOSE_RESOURCES_DIR/strings.xml" <<EOL
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="${CLEAN_FEATURE_NAME}_button_new">New $CLEAN_FEATURE_NAME</string>
    <string name="${CLEAN_FEATURE_NAME}_error_load_failed">Could not load $CLEAN_FEATURE_NAME</string>
</resources>
EOL

cat > "$PRESENTATION_DIR/di/${CAMEL_CASE_FEATURE_NAME}PresentationModule.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.di

import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.vm.${CAMEL_CASE_FEATURE_NAME}ViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val ${CLEAN_FEATURE_NAME}PresentationModule = module {
    viewModelOf(::${CAMEL_CASE_FEATURE_NAME}ViewModel)
}
EOL

echo "Generating iOS view controller wrapper..."

cat > "$IOS_PRESENTATION_DIR/${CAMEL_CASE_FEATURE_NAME}ScreenViewController.kt" <<EOL
package dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.viewcontroller

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.ui.${CAMEL_CASE_FEATURE_NAME}Screen
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.vm.${CAMEL_CASE_FEATURE_NAME}ViewModel
import platform.UIKit.UIViewController

/**
 * Wraps the shared Compose Multiplatform [${CAMEL_CASE_FEATURE_NAME}Screen] in a UIViewController so it can be
 * embedded from SwiftUI via \`UIViewControllerRepresentable\` (see ComposeViewController.swift in iosApp).
 */
@Suppress("FunctionName", "unused")
fun ${CAMEL_CASE_FEATURE_NAME}ScreenViewController(viewModel: ${CAMEL_CASE_FEATURE_NAME}ViewModel): UIViewController =
    ComposeUIViewController {
        ${CAMEL_CASE_FEATURE_NAME}Root(viewModel = viewModel)
    }

@Composable
private fun ${CAMEL_CASE_FEATURE_NAME}Root(viewModel: ${CAMEL_CASE_FEATURE_NAME}ViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ${CAMEL_CASE_FEATURE_NAME}Screen(
        state = state,
        onUiAction = { viewModel.onUiAction(it) },
        onUiEventConsume = { viewModel.onUiEventConsumed(it) },
        modifier = modifier,
    )
}
EOL

# --- Wiring ---------------------------------------------------------------

insert_before_last_match() {
    local file="$1" pattern="$2" content_file="$3"
    local line_num
    # The `|| true` keeps a no-match from tripping `set -e` via the pipefail'd pipeline, so the
    # `-z` check below runs and reports it instead of the script dying silently mid-wiring.
    line_num=$(grep -n -E "$pattern" "$file" | tail -1 | cut -d: -f1 || true)
    if [ -z "$line_num" ]; then
        echo "Warning: anchor pattern not found in $file - add the wiring manually:"
        cat "$content_file"
        return 1
    fi
    { head -n "$((line_num - 1))" "$file"; cat "$content_file"; tail -n +"$line_num" "$file"; } > "$file.tmp" && mv "$file.tmp" "$file"
}

insert_after_last_match() {
    local file="$1" pattern="$2" content_file="$3"
    local line_num
    line_num=$(grep -n -E "$pattern" "$file" | tail -1 | cut -d: -f1 || true)
    if [ -z "$line_num" ]; then
        echo "Warning: anchor pattern not found in $file - add the wiring manually:"
        cat "$content_file"
        return 1
    fi
    { head -n "$line_num" "$file"; cat "$content_file"; tail -n +"$((line_num + 1))" "$file"; } > "$file.tmp" && mv "$file.tmp" "$file"
}

echo "Wiring settings.gradle.kts..."
TMP_SETTINGS=$(mktemp)
cat > "$TMP_SETTINGS" <<EOL
// Feature: $FEATURE_NAME (shared Compose Multiplatform UI + shared VM)
include(":shared:feature:$FEATURE_NAME:domain")
include(":shared:feature:$FEATURE_NAME:data")
include(":shared:feature:$FEATURE_NAME:presentation")

EOL
insert_before_last_match "settings.gradle.kts" "// Umbrella - combines everything into one XCFramework for iOS" "$TMP_SETTINGS"
rm -f "$TMP_SETTINGS"

echo "Wiring shared/umbrella/build.gradle.kts..."
TMP_EXPORTS=$(mktemp)
cat > "$TMP_EXPORTS" <<EOL
            export(projects.shared.feature.$PROJECT_ACCESSOR_NAME.domain)
            export(projects.shared.feature.$PROJECT_ACCESSOR_NAME.presentation)
EOL
insert_after_last_match "shared/umbrella/build.gradle.kts" "^ +export\(projects\.shared\." "$TMP_EXPORTS"
rm -f "$TMP_EXPORTS"

TMP_DEPS=$(mktemp)
cat > "$TMP_DEPS" <<EOL
            api(projects.shared.feature.$PROJECT_ACCESSOR_NAME.domain)
            api(projects.shared.feature.$PROJECT_ACCESSOR_NAME.presentation)
            implementation(projects.shared.feature.$PROJECT_ACCESSOR_NAME.data)
EOL
insert_after_last_match "shared/umbrella/build.gradle.kts" "^ +(api|implementation)\(projects\.shared\." "$TMP_DEPS"
rm -f "$TMP_DEPS"

echo "Wiring AppModules.kt..."
APP_MODULES_FILE="shared/umbrella/src/commonMain/kotlin/dev/whole30journal/umbrella/di/AppModules.kt"

TMP_IMPORTS=$(mktemp)
cat > "$TMP_IMPORTS" <<EOL
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.data.di.${CLEAN_FEATURE_NAME}DataModule
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.domain.di.${CLEAN_FEATURE_NAME}DomainModule
import dev.whole30journal.feature.$CLEAN_FEATURE_NAME.presentation.di.${CLEAN_FEATURE_NAME}PresentationModule
EOL
# Anchored on the always-present koin import (rather than an existing "dev.whole30journal.feature."
# import) so this also works when this is the very first feature being wired in.
insert_before_last_match "$APP_MODULES_FILE" "^import org\.koin\.core\.module\.Module$" "$TMP_IMPORTS"
rm -f "$TMP_IMPORTS"

TMP_LIST=$(mktemp)
cat > "$TMP_LIST" <<EOL
    ${CLEAN_FEATURE_NAME}DomainModule,
    ${CLEAN_FEATURE_NAME}DataModule,
    ${CLEAN_FEATURE_NAME}PresentationModule,
EOL
if grep -q "^val appModules: List<Module> = emptyList()$" "$APP_MODULES_FILE"; then
    # First feature being wired in - turn the placeholder emptyList() into a real listOf(...).
    TMP_APPMODULES=$(mktemp)
    sed 's/^val appModules: List<Module> = emptyList()$/val appModules: List<Module> = listOf(/' "$APP_MODULES_FILE" > "$TMP_APPMODULES"
    cat "$TMP_LIST" >> "$TMP_APPMODULES"
    echo ")" >> "$TMP_APPMODULES"
    mv "$TMP_APPMODULES" "$APP_MODULES_FILE"
else
    insert_before_last_match "$APP_MODULES_FILE" "^\)$" "$TMP_LIST"
fi
rm -f "$TMP_LIST"

echo ""
echo "Feature boilerplate created successfully for: $FEATURE_NAME"
echo ""
echo "Modules created:"
echo "  shared/feature/$FEATURE_NAME/domain"
echo "  shared/feature/$FEATURE_NAME/data"
echo "  shared/feature/$FEATURE_NAME/presentation"
echo ""
echo "Wired into:"
echo "  settings.gradle.kts"
echo "  shared/umbrella/build.gradle.kts (exports + dependencies)"
echo "  shared/umbrella/.../di/AppModules.kt (Koin modules)"
echo ""
echo "Manual next steps (not done by this script - same boundary as the original tool):"
echo "  1. Replace the placeholder sample data in Fake${CAMEL_CASE_FEATURE_NAME}Repository.kt"
echo "  2. Wire ${CAMEL_CASE_FEATURE_NAME}Screen into androidApp (a <Feature>Route.kt + a destination in App.kt)"
echo "  3. Wire ${CAMEL_CASE_FEATURE_NAME}ScreenViewController into iosApp (a SwiftUI view + ViewModelFactory + a tab in ContentView.swift)"
echo "  4. Add translations under composeResources/values-<lang>/strings.xml as needed (see ARCHITECTURE.md)"
