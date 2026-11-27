package com.propodcast.app.core.commonwidgets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.userMessage

/**
 * Paged podcasts grid (one column on phones, more on wide screens) with full-screen loading,
 * error with retry and [emptyMessage] states (both with an optional [emptyAction]), pull to refresh and an
 * append footer.
 */
@Composable
fun PagedPodcastGrid(
    podcasts: LazyPagingItems<Podcast>,
    favoriteIds: Set<String>,
    emptyMessage: String,
    onPodcastClick: (Podcast) -> Unit,
    onToggleFavorite: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    gridTestTag: String = PAGED_PODCAST_GRID_TAG,
    /** Shown under the error / empty message when there is nothing to list. */
    emptyAction: MessageAction? = null,
) {
    val refresh = podcasts.loadState.refresh
    when {
        podcasts.itemCount == 0 && refresh is LoadState.Loading -> LoadingState(modifier)
        podcasts.itemCount == 0 && refresh is LoadState.Error ->
            MessageState(
                message = refresh.error.userMessage(),
                modifier = modifier,
                onRetry = podcasts::retry,
                secondaryAction = emptyAction,
            )
        podcasts.itemCount == 0 && refresh is LoadState.NotLoading && podcasts.loadState.append.endOfPaginationReached ->
            MessageState(
                message = emptyMessage,
                modifier = modifier,
                onRetry = podcasts::refresh,
                secondaryAction = emptyAction,
            )
        else -> PullToRefreshBox(
            isRefreshing = refresh is LoadState.Loading,
            onRefresh = podcasts::refresh,
            modifier = modifier.fillMaxSize(),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 360.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(gridTestTag),
            ) {
                items(
                    count = podcasts.itemCount,
                    key = podcasts.itemKey { it.id },
                    contentType = podcasts.itemContentType { "podcast" },
                ) { index ->
                    val podcast = podcasts[index] ?: return@items
                    PodcastListItem(
                        podcast = podcast,
                        isFavorite = podcast.id in favoriteIds,
                        onClick = { onPodcastClick(podcast) },
                        onToggleFavorite = { onToggleFavorite(podcast) },
                        onShare = podcast.listennotesUrl?.let { { onShare(podcast) } },
                    )
                }
                pagingAppendFooter(podcasts)
            }
        }
    }
}

const val PAGED_PODCAST_GRID_TAG = "podcastGrid"
