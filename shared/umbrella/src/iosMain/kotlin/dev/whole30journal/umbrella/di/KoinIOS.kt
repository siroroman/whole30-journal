@file:OptIn(BetaInteropApi::class)

package dev.whole30journal.umbrella.di

import dev.whole30journal.core.database.di.iosDatabaseModule
import dev.whole30journal.feature.dayentry.domain.usecase.SaveDayEntryUseCase
import dev.whole30journal.feature.program.domain.usecase.ConfigureProgramUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import dev.whole30journal.umbrella.seed.SampleDataSeeder
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import kotlinx.coroutines.runBlocking
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

/**
 * iOS entry point for Koin. [initKoinIos] must run before any Swift code touches KoinResolver -
 * see KoinStarter.swift in iosApp, which calls it from the App's init. Unlike Android (which relies
 * on Koin's GlobalContext), iOS keeps an explicit [KoinApplication] reference so KoinResolver never
 * has to assume a global singleton has been set up correctly.
 */
@Suppress("Unused")
object KoinIOS {
    private var koinApp: KoinApplication? = null

    fun initKoinIos(doOnStartup: () -> Unit) {
        val app = koinApplication {
            modules(appModules + iosDatabaseModule)
        }
        koinApp = app

        runBlocking {
            runCatching {
                SampleDataSeeder(
                    getProgram = app.koin.get<GetProgramUseCase>(),
                    configureProgram = app.koin.get<ConfigureProgramUseCase>(),
                    saveDayEntry = app.koin.get<SaveDayEntryUseCase>(),
                ).seedIfNeeded()
            }
        }

        doOnStartup()
    }

    fun getKoinApplication(): KoinApplication =
        koinApp ?: error("KoinApplication not initialized - call KoinIOS.initKoinIos() first")
}

/** Bridges Swift's `SomeViewModel.self` (an ObjC class) back to Koin's Kotlin KClass lookup. */
fun Koin.get(objCClass: ObjCClass): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, null)
}
