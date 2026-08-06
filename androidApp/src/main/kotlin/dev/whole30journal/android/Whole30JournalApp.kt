package dev.whole30journal.android

import android.app.Application
import dev.whole30journal.core.database.di.androidDatabaseModule
import dev.whole30journal.feature.dayentry.domain.usecase.SaveDayEntryUseCase
import dev.whole30journal.feature.program.domain.usecase.ConfigureProgramUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import dev.whole30journal.umbrella.di.appModules
import dev.whole30journal.umbrella.seed.SampleDataSeeder
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Whole30JournalApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val koinApp = startKoin {
            androidContext(this@Whole30JournalApp)
            modules(appModules + androidDatabaseModule)
        }

        runBlocking {
            runCatching {
                SampleDataSeeder(
                    getProgram = koinApp.koin.get<GetProgramUseCase>(),
                    configureProgram = koinApp.koin.get<ConfigureProgramUseCase>(),
                    saveDayEntry = koinApp.koin.get<SaveDayEntryUseCase>(),
                ).seedIfNeeded()
            }
        }
    }
}
