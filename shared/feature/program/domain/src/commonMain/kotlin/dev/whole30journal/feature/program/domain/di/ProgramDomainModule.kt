package dev.whole30journal.feature.program.domain.di

import dev.whole30journal.feature.program.domain.usecase.ConfigureProgramUseCase
import dev.whole30journal.feature.program.domain.usecase.GetProgramUseCase
import dev.whole30journal.feature.program.domain.usecase.ObserveProgramUseCase
import org.koin.dsl.module

val programDomainModule = module {
    factory { GetProgramUseCase(get()) }
    factory { ObserveProgramUseCase(get()) }
    factory { ConfigureProgramUseCase(get()) }
}
