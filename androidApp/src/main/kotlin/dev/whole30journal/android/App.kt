package dev.whole30journal.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

/**
 * Root of the app. No feature is wired in yet - see ARCHITECTURE.md and
 * scripts/create_kmp_feature_boilerplate.sh for how to scaffold one.
 */
@Composable
fun App() {
    MaterialTheme {
        Scaffold { }
    }
}
