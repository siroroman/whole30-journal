package dev.whole30journal.core.database

import kotlinx.coroutines.CoroutineDispatcher

/** Shared by every repository built on [Whole30Database] - the underlying SQLite connection isn't
 * safe for concurrent multi-threaded use, so all DB access across the app is confined to one
 * logical thread here, rather than each repository confining only its own calls (which wouldn't
 * mutually exclude a second repository's independently-confined calls on the same connection). */
expect val databaseDispatcher: CoroutineDispatcher
