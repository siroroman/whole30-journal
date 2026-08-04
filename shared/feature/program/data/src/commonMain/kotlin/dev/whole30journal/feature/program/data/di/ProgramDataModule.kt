package dev.whole30journal.feature.program.data.di

import dev.whole30journal.feature.program.data.ProgramRepositoryImpl
import dev.whole30journal.feature.program.domain.repository.ProgramRepository
import org.koin.dsl.module

val programDataModule = module {
    single<ProgramRepository> { ProgramRepositoryImpl(get()) }
}
