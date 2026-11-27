package com.propodcast.app.feature.details

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_add_to_favorites
import com.propodcast.app.resources.action_remove_from_favorites
import com.propodcast.app.resources.action_share
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.BackButton
import com.propodcast.app.core.commonwidgets.LoadingState
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.core.ui.message
import com.propodcast.app.core.ui.sharedArtwork
import com.propodcast.app.feature.details.widgets.episodesSection
import com.propodcast.app.feature.details.widgets.PodcastDetailsHeader
import com.propodcast.app.feature.details.widgets.loadMoreFooter
import com.propodcast.app.feature.details.widgets.recommendationsSection

/** Stateless details content, also used by UI tests. */
@Composable
fun PodcastDetailsContent(
    state: PodcastDetailsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onTogglePodcastFavorite: () -> Unit,
    onSharePodcast: (Podcast) -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onToggleEpisodeFavorite: (Episode) -> Unit,
    onShareEpisode: (Episode) -> Unit,
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    preview: DetailsPreview = DetailsPreview(),
    downloads: EpisodeDownloadActions = EpisodeDownloadActions(),
) {
    val loaded = state.content as? DetailsContent.Loaded
    val artworkModifier = Modifier.sharedArtwork(preview.artworkKey)
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                // No title: the header below already names the podcast in large type.
                title = {},
                navigationIcon = { BackButton(onClick = onBack) },
                actions = {
                    if (loaded != null) {
                        IconButton(onClick = onTogglePodcastFavorite) {
                            Icon(
                                imageVector = if (state.isPodcastFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = stringResource(
                                    if (state.isPodcastFavorite) {
                                        Res.string.action_remove_from_favorites
                                    } else {
                                        Res.string.action_add_to_favorites
                                    },
                                ),
                            )
                        }
                        if (loaded.podcast.listennotesUrl != null) {
                            IconButton(onClick = { onSharePodcast(loaded.podcast) }) {
                                Icon(Icons.Filled.Share, contentDescription = stringResource(Res.string.action_share))
                            }
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (val content = state.content) {
            DetailsContent.Loading -> if (preview.title != null) {
                // Artwork and title from the list right away (the artwork flight lands here), the rest loads below.
                Column(contentModifier.padding(top = 16.dp)) {
                    PodcastDetailsHeader(preview.toPodcast(), artworkModifier = artworkModifier)
                    LoadingState(Modifier.weight(1f))
                }
            } else {
                LoadingState(contentModifier)
            }
            is DetailsContent.Error -> MessageState(content.error.message(), contentModifier, onRetry = onRetry)
            is DetailsContent.Loaded -> BoxWithConstraints(contentModifier) {
                val header: LazyListScope.() -> Unit = {
                    item(key = "header") {
                        val podcast = content.podcast
                        // Keep the list's artwork so the image doesn't change once the details arrive.
                        PodcastDetailsHeader(
                            podcast = podcast.copy(thumbnailUrl = preview.artworkUrl ?: podcast.thumbnailUrl),
                            artworkModifier = artworkModifier,
                        )
                    }
                    recommendationsSection(state.recommendations, onPodcastClick)
                }
                val episodes: LazyListScope.() -> Unit = {
                    episodesSection(
                        content, state.favoriteEpisodeIds, onEpisodeClick, onToggleEpisodeFavorite, onShareEpisode, downloads,
                    )
                    loadMoreFooter(content, onLoadMore)
                }
                if (maxWidth >= TWO_PANE_MIN_WIDTH) {
                    // Wide: the podcast on the left, its episodes on the right, each scrolling on its own.
                    Row(Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(LEFT_PANE_WEIGHT).fillMaxHeight(),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            content = header,
                        )
                        VerticalDivider()
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f - LEFT_PANE_WEIGHT)
                                .fillMaxHeight()
                                .testTag(DETAILS_LIST_TAG),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            content = episodes,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag(DETAILS_LIST_TAG),
                        contentPadding = PaddingValues(vertical = 16.dp),
                    ) {
                        header()
                        episodes()
                    }
                }
            }
        }
    }
}

/** From this content width the details split into two panes (tablets, landscape). */
private val TWO_PANE_MIN_WIDTH = 840.dp
private const val LEFT_PANE_WEIGHT = 0.42f
