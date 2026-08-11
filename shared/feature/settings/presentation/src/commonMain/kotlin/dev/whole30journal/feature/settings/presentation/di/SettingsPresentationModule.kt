package dev.whole30journal.feature.settings.presentation.di

import dev.whole30journal.feature.settings.presentation.vm.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsPresentationModule = module {
    viewModelOf(::SettingsViewModel)
}
