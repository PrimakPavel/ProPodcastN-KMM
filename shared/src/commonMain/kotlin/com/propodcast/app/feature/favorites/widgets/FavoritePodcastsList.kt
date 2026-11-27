package com.propodcast.app.feature.favorites.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.favorites_podcasts_empty
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.commonwidgets.PodcastListItem
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.feature.favorites.FAVORITE_PODCASTS_LIST_TAG

@Composable
internal fun FavoritePodcastsList(
    podcasts: List<Podcast>,
    onPodcastClick: (Podcast) -> Unit,
    onRemove: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (podcasts.isEmpty()) {
        MessageState(stringResource(Res.string.favorites_podcasts_empty), modifier)
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = LIST_COLUMN_MIN_WIDTH),
        modifier = modifier.testTag(FAVORITE_PODCASTS_LIST_TAG),
    ) {
        items(podcasts, key = { it.id }) { podcast ->
            SwipeToRemove(onRemove = { onRemove(podcast) }, modifier = removableItemModifier()) {
                PodcastListItem(
                    podcast = podcast,
                    isFavorite = true,
                    onClick = { onPodcastClick(podcast) },
                    onToggleFavorite = { onRemove(podcast) },
                    onShare = podcast.listennotesUrl?.let { { onShare(podcast) } },
                )
            }
        }
    }
}
