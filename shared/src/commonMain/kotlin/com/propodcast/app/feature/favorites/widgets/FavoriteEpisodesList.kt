package com.propodcast.app.feature.favorites.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.favorites_episodes_empty
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.EpisodeListItem
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.feature.favorites.FAVORITE_EPISODES_LIST_TAG

@Composable
internal fun FavoriteEpisodesList(
    episodes: List<Episode>,
    onEpisodeClick: (Episode) -> Unit,
    onRemove: (Episode) -> Unit,
    onShare: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    downloads: EpisodeDownloadActions = EpisodeDownloadActions(),
) {
    if (episodes.isEmpty()) {
        MessageState(stringResource(Res.string.favorites_episodes_empty), modifier)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = LIST_COLUMN_MIN_WIDTH),
        modifier = modifier.testTag(FAVORITE_EPISODES_LIST_TAG),
    ) {
        items(episodes, key = { it.id }) { episode ->
            SwipeToRemove(onRemove = { onRemove(episode) }, modifier = removableItemModifier()) {
                EpisodeListItem(
                    episode = episode,
                    isFavorite = true,
                    onClick = { onEpisodeClick(episode) },
                    onToggleFavorite = { onRemove(episode) },
                    onShare = episode.listennotesUrl?.let { { onShare(episode) } },
                    downloads = downloads,
                )
            }
        }
    }
}
