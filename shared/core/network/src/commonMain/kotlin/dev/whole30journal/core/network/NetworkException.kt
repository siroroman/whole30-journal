package dev.whole30journal.core.network

/** Typed failure reported via [Result.failure] by every [NetworkClient] call. */
sealed class NetworkException(message: String, cause: Throwable) : Exception(message, cause) {

    /** No usable connection - offline, DNS failure, connection refused, or dropped mid-request. */
    class NoConnection(cause: Throwable) : NetworkException("No network connection", cause)

    /** The request (or its connect/socket phase) exceeded its timeout. */
    class Timeout(cause: Throwable) : NetworkException("Request timed out", cause)

    /** Server responded with a non-2xx status. [body] is the raw response text, if it could be read. */
    class Http(val statusCode: Int, val body: String?, cause: Throwable) :
        NetworkException("Request failed with status $statusCode", cause)

    /** The request or response body couldn't be (de)serialized. */
    class Serialization(cause: Throwable) : NetworkException("Failed to (de)serialize payload", cause)

    /** Anything that doesn't fall into the categories above. */
    class Unknown(cause: Throwable) : NetworkException(cause.message ?: "Unknown network error", cause)
}
