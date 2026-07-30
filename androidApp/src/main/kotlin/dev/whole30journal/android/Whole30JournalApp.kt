package dev.whole30journal.android

import android.app.Application
import dev.whole30journal.umbrella.di.appModules
import org.koin.core.context.startKoin

class Whole30JournalApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModules)
        }
    }
}
