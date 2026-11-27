package com.propodcast.app.feature.favorites

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.favorites_title
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.LoadingState
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.EpisodeDownloadActions
import com.propodcast.app.feature.favorites.widgets.DownloadsList
import com.propodcast.app.feature.favorites.widgets.FavoriteEpisodesList
import com.propodcast.app.feature.favorites.widgets.FavoritePodcastsList
import com.propodcast.app.feature.favorites.widgets.FavoritesTabRow
import kotlinx.coroutines.launch

/** Stateless favorites content: Podcasts / Episodes / Downloaded tabs, rows removable by swipe or heart. Also used by UI tests. */
@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onPodcastClick: (Podcast) -> Unit,
    onRemovePodcast: (Podcast) -> Unit,
    onSharePodcast: (Podcast) -> Unit,
    onEpisodeClick: (Episode) -> Unit,
    onRemoveEpisode: (Episode) -> Unit,
    onShareEpisode: (Episode) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    initialTab: FavoritesTab = FavoritesTab.PODCASTS,
    downloads: EpisodeDownloadActions = EpisodeDownloadActions(),
) {
    val pagerState = rememberPagerState(initialPage = initialTab.ordinal) { FavoritesTab.entries.size }
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(Res.string.favorites_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            FavoritesTabRow(
                selected = FavoritesTab.entries[pagerState.currentPage],
                counts = mapOf(
                    FavoritesTab.PODCASTS to state.podcasts.size,
                    FavoritesTab.EPISODES to state.episodes.size,
                    FavoritesTab.DOWNLOADS to state.downloads.size,
                ),
                onSelect = { tab -> scope.launch { pagerState.animateScrollToPage(tab.ordinal) } },
            )
            if (state.isLoading) {
                LoadingState()
            } else {
                // Tabs switch by tap only: a horizontal swipe on a row removes it, so the pager must not take it.
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = false) { page ->
                    when (FavoritesTab.entries[page]) {
                        FavoritesTab.PODCASTS -> FavoritePodcastsList(
                            podcasts = state.podcasts,
                            onPodcastClick = onPodcastClick,
                            onRemove = onRemovePodcast,
                            onShare = onSharePodcast,
                            modifier = Modifier.fillMaxSize(),
                        )
                        FavoritesTab.EPISODES -> FavoriteEpisodesList(
                            episodes = state.episodes,
                            onEpisodeClick = onEpisodeClick,
                            onRemove = onRemoveEpisode,
                            onShare = onShareEpisode,
                            downloads = downloads,
                            modifier = Modifier.fillMaxSize(),
                        )
                        FavoritesTab.DOWNLOADS -> DownloadsList(
                            downloads = state.downloads,
                            actions = downloads,
                            onEpisodeClick = onEpisodeClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
