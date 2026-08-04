package dev.whole30journal.core.database

import kotlinx.coroutines.CancellationException

/** Shared by every repository built on [Whole30Database] - [runCatching] alone would catch and box
 * [CancellationException] into a [Result.failure] instead of letting it propagate, breaking
 * structured-concurrency cancellation. This rethrows it instead. */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    runCatching(block).onFailure { if (it is CancellationException) throw it }
