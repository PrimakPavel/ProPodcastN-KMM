package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_download_done
import com.propodcast.app.resources.download_state_downloaded
import com.propodcast.app.resources.download_state_downloading
import com.propodcast.app.resources.download_state_downloading_unknown
import com.propodcast.app.resources.download_state_failed
import com.propodcast.app.resources.download_state_queued
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.EpisodeDownloadState

/**
 * Small download state mark for episode rows: a progress ring while queued / downloading, a filled check
 * when downloaded, a retry mark on failure. Not a button: the actions live in the row's menu.
 */
@Composable
fun DownloadIndicator(
    state: EpisodeDownloadState,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    val description = downloadStateLabel(state)
    val semantics = modifier
        .size(size)
        .semantics { contentDescription = description }
    when (state.status) {
        DownloadStatus.QUEUED -> CircularProgressIndicator(modifier = semantics, strokeWidth = 2.dp)
        DownloadStatus.DOWNLOADING -> {
            val progress = state.progress
            if (progress == null) {
                CircularProgressIndicator(modifier = semantics, strokeWidth = 2.dp)
            } else {
                CircularProgressIndicator(progress = { progress }, modifier = semantics, strokeWidth = 2.dp)
            }
        }
        DownloadStatus.DOWNLOADED -> Icon(
            painter = painterResource(Res.drawable.ic_download_done),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = semantics,
        )
        DownloadStatus.FAILED -> Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = semantics,
        )
    }
}

/** "Downloading 45%", "Downloaded", … */
@Composable
fun downloadStateLabel(state: EpisodeDownloadState): String = when (state.status) {
    DownloadStatus.QUEUED -> stringResource(Res.string.download_state_queued)
    DownloadStatus.DOWNLOADING -> state.progress
        ?.let { stringResource(Res.string.download_state_downloading, (it * 100).toInt()) }
        ?: stringResource(Res.string.download_state_downloading_unknown)
    DownloadStatus.DOWNLOADED -> stringResource(Res.string.download_state_downloaded)
    DownloadStatus.FAILED -> stringResource(Res.string.download_state_failed)
}
