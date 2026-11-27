package com.propodcast.app.core.common

import com.propodcast.app.core.testing.ListenNotesTestClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class AppErrorTest {

    @Test
    fun httpCodes_mapToAppErrors() = runTest {
        val expected = mapOf(
            400 to AppError.Client(400),
            401 to AppError.Unauthorized,
            403 to AppError.Client(403),
            404 to AppError.NotFound,
            429 to AppError.TooManyRequests,
            500 to AppError.Server(500),
            503 to AppError.Server(503),
        )
        val server = ListenNotesTestClient()
        expected.forEach { (code, error) ->
            server.enqueue(code)
            val result = apiCall { server.api.languages() }
            assertEquals(error, (result.exceptionOrNull() as AppException).error, "HTTP $code")
        }
    }

    @Test
    fun ioException_isNetworkError() = runTest {
        val result = apiCall<Unit> { throw IOException("offline") }
        assertEquals(AppError.Network, (result.exceptionOrNull() as AppException).error)
    }

    @Test
    fun malformedBody_isUnknownError() = runTest {
        val server = ListenNotesTestClient()
        server.enqueue(200, "not json")
        val result = apiCall { server.api.languages() }
        assertTrue((result.exceptionOrNull() as AppException).error is AppError.Unknown)
    }
}
