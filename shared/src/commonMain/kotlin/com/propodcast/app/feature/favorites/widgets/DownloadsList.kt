package com.propodcast.app.feature.favorites.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.favorites_downloads_empty
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownload
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.feature.favorites.DOWNLOADS_LIST_TAG

/**
 * The Downloaded tab: running downloads first, then finished ones. A swipe cancels a running download or
 * deletes a finished / failed one (both with a snackbar to take it back); a tap plays the episode.
 */
@Composable
internal fun DownloadsList(
    downloads: List<EpisodeDownload>,
    actions: EpisodeDownloadActions,
    onEpisodeClick: (Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (downloads.isEmpty()) {
        MessageState(stringResource(Res.string.favorites_downloads_empty), modifier)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = LIST_COLUMN_MIN_WIDTH),
        modifier = modifier.testTag(DOWNLOADS_LIST_TAG),
    ) {
        items(downloads, key = { it.episode.id }) { download ->
            val episode = download.episode
            val cancel = { actions.onCancel?.invoke(episode) }
            SwipeToRemove(
                onRemove = { if (download.state.isActive) cancel() else actions.onDelete?.invoke(episode) },
                modifier = removableItemModifier(),
            ) {
                DownloadListItem(
                    download = download,
                    onClick = { onEpisodeClick(episode) },
                    onCancel = { cancel() },
                    onRetry = { actions.onDownload?.invoke(episode) },
                )
            }
        }
    }
}
