package dev.whole30journal.core.network.di

import dev.whole30journal.core.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { createHttpClient() }
}
