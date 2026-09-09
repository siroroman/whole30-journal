package dev.whole30journal.core.utils.di

import dev.whole30journal.core.utils.MealPhotoStorage
import org.koin.dsl.module

val iosUtilsModule = module {
    single { MealPhotoStorage() }
}
