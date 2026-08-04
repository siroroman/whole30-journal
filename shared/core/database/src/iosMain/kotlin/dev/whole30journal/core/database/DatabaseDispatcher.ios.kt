package dev.whole30journal.core.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Dispatchers.IO isn't public API on Kotlin/Native (unlike JVM/Android), so this falls back to
// Dispatchers.Default - still correctly serialized via limitedParallelism(1), just not isolated
// from the CPU-bound thread pool the way the JVM/Android actual is.
actual val databaseDispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
