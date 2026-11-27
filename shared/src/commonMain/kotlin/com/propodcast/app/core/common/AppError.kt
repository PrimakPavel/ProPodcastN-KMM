package com.propodcast.app.core.common

import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * Errors the UI knows how to explain to the user.
 * HTTP codes follow the Listen Notes API v2 docs (400, 401, 404, 429, 5xx).
 */
sealed interface AppError {
    /** 401: wrong API key, or the Listen Notes account is suspended. */
    data object Unauthorized : AppError

    /** 404: the podcast / episode does not exist. */
    data object NotFound : AppError

    /**
     * 429: either the FREE plan monthly quota is used up, or requests are sent too fast
     * (rate limit, any plan). The API does not say which; retrying later may help.
     */
    data object TooManyRequests : AppError

    /** Other 4xx (e.g. 400: missing or invalid parameters): a problem with our request. */
    data class Client(val code: Int) : AppError

    /** 5xx: unexpected error on the Listen Notes side. */
    data class Server(val code: Int) : AppError

    /** No connection, timeout, DNS, etc. */
    data object Network : AppError

    /** Response could not be parsed, or anything else. */
    data class Unknown(val cause: Throwable?) : AppError
}

/** Carries an [AppError] through Result / Paging error channels. */
class AppException(val error: AppError, cause: Throwable? = null) : Exception(error.toString(), cause)

fun Throwable.toAppError(): AppError = when (this) {
    is AppException -> error
    is ResponseException -> when (val code = response.status.value) {
        401 -> AppError.Unauthorized
        404 -> AppError.NotFound
        429 -> AppError.TooManyRequests
        in 500..599 -> AppError.Server(code)
        in 400..499 -> AppError.Client(code)
        else -> AppError.Unknown(this)
    }
    // Ktor's network errors (timeouts, no connection, Darwin NSURLError, OkHttp IOException) are IOExceptions.
    is IOException -> AppError.Network
    is SerializationException -> AppError.Unknown(this)
    else -> AppError.Unknown(this)
}

/** Runs a network call; any failure is converted to [AppException]. Cancellation is rethrown. */
suspend fun <T> apiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(AppException(e.toAppError(), e))
}
