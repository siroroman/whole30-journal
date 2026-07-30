package dev.whole30journal.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Base class for feature `data`-layer repositories that talk to a JSON HTTP API over [httpClient].
 * Each verb below catches Ktor/IO exceptions and reports them as a typed [NetworkException] through
 * [Result.failure], so callers can branch on failure kind without depending on Ktor themselves.
 */
abstract class NetworkClient(@PublishedApi internal val httpClient: HttpClient) {

    protected suspend inline fun <reified T> get(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = runCatchingNetworkErrors { httpClient.get(url, block).body() }

    protected suspend inline fun <reified T> post(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = runCatchingNetworkErrors { httpClient.post(url, block).body() }

    protected suspend inline fun <reified T> put(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = runCatchingNetworkErrors { httpClient.put(url, block).body() }

    protected suspend inline fun <reified T> patch(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = runCatchingNetworkErrors { httpClient.patch(url, block).body() }

    protected suspend inline fun <reified T> delete(
        url: String,
        crossinline block: HttpRequestBuilder.() -> Unit = {},
    ): Result<T> = runCatchingNetworkErrors { httpClient.delete(url, block).body() }
}

@PublishedApi
internal suspend fun <T> runCatchingNetworkErrors(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toNetworkException())
    }

private suspend fun Exception.toNetworkException(): NetworkException = when (this) {
    is HttpRequestTimeoutException,
    is ConnectTimeoutException,
    is SocketTimeoutException -> NetworkException.Timeout(this)

    is ResponseException -> NetworkException.Http(
        statusCode = response.status.value,
        body = runCatching { response.bodyAsText() }.getOrNull(),
        cause = this,
    )

    is NoTransformationFoundException,
    is SerializationException -> NetworkException.Serialization(this)

    is IOException -> NetworkException.NoConnection(this)

    else -> NetworkException.Unknown(this)
}
