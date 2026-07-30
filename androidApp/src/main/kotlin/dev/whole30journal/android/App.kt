package dev.whole30journal.android

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.whole30journal.android.example.ExampleRoute

/**
 * Root of the app. Renders [ExampleRoute], the example feature scaffolded by
 * create_kmp_feature_boilerplate.sh - see ARCHITECTURE.md for the pattern it follows.
 */
@Composable
fun App() {
    MaterialTheme {
        Scaffold { contentPadding ->
            ExampleRoute(modifier = Modifier.fillMaxSize().padding(contentPadding))
        }
    }
}
