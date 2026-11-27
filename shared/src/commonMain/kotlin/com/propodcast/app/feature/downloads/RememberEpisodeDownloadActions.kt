package com.propodcast.app.feature.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.core.ui.rememberNotificationPermissionRequester

/**
 * Download states and actions for a screen's episode lists. A download first asks for the notification
 * permission (once), so its progress shows in the shade. Pair with [CellularDownloadPrompt] and a
 * SnackbarEffect for [DownloadsViewModel.snackbarEvents].
 */
@Composable
fun rememberEpisodeDownloadActions(viewModel: DownloadsViewModel): EpisodeDownloadActions {
    val states by viewModel.states.collectAsStateWithLifecycle()
    val withNotificationPermission = rememberNotificationPermissionRequester()
    return EpisodeDownloadActions(
        states = states,
        onDownload = { episode -> withNotificationPermission { viewModel.download(episode) } },
        onCancel = viewModel::cancel,
        onDelete = viewModel::delete,
    )
}
