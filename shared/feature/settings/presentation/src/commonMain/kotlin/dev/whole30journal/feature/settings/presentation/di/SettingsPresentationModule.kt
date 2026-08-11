@file:OptIn(ExperimentalTime::class)

package dev.whole30journal.feature.settings.presentation.di

import dev.whole30journal.core.utils.DateFormatter
import dev.whole30journal.feature.settings.presentation.vm.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

val settingsPresentationModule = module {
    factory<Clock> { Clock.System }
    factoryOf(::DateFormatter)
    viewModelOf(::SettingsViewModel)
}
