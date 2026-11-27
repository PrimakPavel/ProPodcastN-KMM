package com.propodcast.app.feature.favorites.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_cancel_download
import com.propodcast.app.resources.download_retry
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.DownloadIndicator
import com.propodcast.app.core.commonwidgets.PodcastImage
import com.propodcast.app.core.commonwidgets.downloadStateLabel
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.ui.fileSizeLabel

/**
 * A row of the Downloaded tab: artwork, title, podcast and the download state ("Downloading 45%",
 * "Downloaded · 62 MB"). Running downloads have ✕ to cancel, failed ones ↻ to retry.
 */
@Composable
internal fun DownloadListItem(
    download: EpisodeDownload,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val episode = download.episode
    val state = download.state
    // An icon button brings its own 12 dp inner space, so the row needs only 4 dp after it; without one
    // the text would run up to the edge, so it gets the same 16 dp as the start.
    val hasAction = state.isActive || state.status == DownloadStatus.FAILED
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = if (hasAction) 4.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PodcastImage(url = episode.thumbnailUrl ?: episode.imageUrl, modifier = Modifier.size(56.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = episode.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            episode.podcastTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                DownloadIndicator(state, size = 14.dp)
                Spacer(Modifier.width(6.dp))
                val label = downloadStateLabel(state)
                val size = state.sizeBytes?.takeIf { state.status == DownloadStatus.DOWNLOADED }
                Text(
                    text = size?.let { "$label · ${fileSizeLabel(it)}" } ?: label,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (state.status == DownloadStatus.FAILED) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        when {
            state.isActive -> IconButton(onClick = onCancel) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.action_cancel_download))
            }
            state.status == DownloadStatus.FAILED -> IconButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(Res.string.download_retry))
            }
        }
    }
}
