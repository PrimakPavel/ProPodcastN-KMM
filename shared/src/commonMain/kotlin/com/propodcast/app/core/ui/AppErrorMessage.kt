package com.propodcast.app.core.ui

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.error_client
import com.propodcast.app.resources.error_network
import com.propodcast.app.resources.error_not_found
import com.propodcast.app.resources.error_server
import com.propodcast.app.resources.error_too_many_requests
import com.propodcast.app.resources.error_unauthorized
import com.propodcast.app.resources.error_unknown
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.common.AppError
import com.propodcast.app.core.common.toAppError

/** User-facing text for an [AppError]. */
@Composable
fun AppError.message(): String = when (this) {
    AppError.Unauthorized -> stringResource(Res.string.error_unauthorized)
    AppError.NotFound -> stringResource(Res.string.error_not_found)
    AppError.TooManyRequests -> stringResource(Res.string.error_too_many_requests)
    is AppError.Client -> stringResource(Res.string.error_client, code)
    is AppError.Server -> stringResource(Res.string.error_server, code)
    AppError.Network -> stringResource(Res.string.error_network)
    is AppError.Unknown -> stringResource(Res.string.error_unknown)
}

/** User-facing text for any throwable coming from the data layer (e.g. a Paging LoadState.Error). */
@Composable
fun Throwable.userMessage(): String = toAppError().message()
