package dev.whole30journal.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val REQUEST_TIMEOUT_MILLIS = 30_000L

/**
 * An [HttpClient] configured with JSON (de)serialization, request/connect/socket timeouts, and
 * request/response logging. The concrete engine (OkHttp on Android, Darwin on iOS) is picked up
 * automatically from whichever engine artifact is on each target's classpath.
 */
fun createHttpClient(): HttpClient = HttpClient {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }

    install(Logging) {
        level = LogLevel.INFO
    }
}
