package com.propodcast.app.feature.player.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_download
import com.propodcast.app.resources.ic_download_done
import com.propodcast.app.resources.action_cancel_download
import com.propodcast.app.resources.action_delete_download
import com.propodcast.app.resources.action_download
import com.propodcast.app.resources.download_retry
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.EpisodeDownloadState

/**
 * Full player download button: ⬇ download, a progress ring with ✕ while downloading (cancels),
 * ✓ when downloaded (deletes, with Undo), ↻ after a failure (retries). [onClick] acts on the current state.
 */
@Composable
internal fun DownloadButton(
    state: EpisodeDownloadState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(onClick = onClick, modifier = modifier) {
        when (state?.status) {
            null -> Icon(
                painter = painterResource(Res.drawable.ic_download),
                contentDescription = stringResource(Res.string.action_download),
            )
            DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING -> Box(contentAlignment = Alignment.Center) {
                val progress = state.progress
                if (progress == null) {
                    CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 2.dp)
                } else {
                    CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
                }
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(Res.string.action_cancel_download),
                    modifier = Modifier.size(18.dp),
                )
            }
            DownloadStatus.DOWNLOADED -> Icon(
                painter = painterResource(Res.drawable.ic_download_done),
                contentDescription = stringResource(Res.string.action_delete_download),
                tint = MaterialTheme.colorScheme.primary,
            )
            DownloadStatus.FAILED -> Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(Res.string.download_retry),
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
