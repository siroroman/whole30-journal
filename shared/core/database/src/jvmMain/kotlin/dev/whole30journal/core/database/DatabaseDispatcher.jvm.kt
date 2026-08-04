package dev.whole30journal.core.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val databaseDispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)
