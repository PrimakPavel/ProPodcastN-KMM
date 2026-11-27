package com.propodcast.app.feature.home

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_filter_list
import com.propodcast.app.resources.action_filters
import com.propodcast.app.resources.action_settings
import com.propodcast.app.resources.best_podcasts_title
import com.propodcast.app.resources.home_empty
import com.propodcast.app.resources.home_go_to_downloads
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.paging.compose.LazyPagingItems
import com.propodcast.app.core.commonwidgets.FloatingHeader
import com.propodcast.app.core.commonwidgets.MessageAction
import com.propodcast.app.core.commonwidgets.PagedPodcastGrid
import com.propodcast.app.core.commonwidgets.QuotaBanner
import com.propodcast.app.core.commonwidgets.rememberFloatingHeaderScrollBehavior
import com.propodcast.app.core.model.ApiQuota
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.feature.filters.FiltersTab
import com.propodcast.app.feature.filters.FiltersUiState
import com.propodcast.app.feature.home.widgets.ActiveFilters

/** Stateless home content, also used by UI tests. */
@Composable
fun HomeContent(
    podcasts: LazyPagingItems<Podcast>,
    favoriteIds: Set<String>,
    quota: ApiQuota?,
    filtersState: FiltersUiState,
    onPodcastClick: (Podcast) -> Unit,
    onToggleFavorite: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
    onOpenFilters: (FiltersTab) -> Unit,
    onClearGenre: () -> Unit,
    onClearRegion: () -> Unit,
    modifier: Modifier = Modifier,
    filtersPinned: Boolean = false,
    /** When set, an empty or failed list offers "Go to downloads" (e.g. offline with downloaded episodes). */
    onOpenDownloads: (() -> Unit)? = null,
    onOpenSettings: () -> Unit = {},
) {
    // Filter chips hide while scrolling down and come back on scroll up (pass filtersPinned = true to keep them).
    val filtersScrollBehavior = rememberFloatingHeaderScrollBehavior(pinned = filtersPinned)
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.best_podcasts_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(Res.string.action_settings))
                    }
                    IconButton(onClick = { onOpenFilters(FiltersTab.GENRE) }) {
                        BadgedBox(badge = { if (filtersState.hasActiveFilters) Badge() }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_filter_list),
                                contentDescription = stringResource(Res.string.action_filters),
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(filtersScrollBehavior.nestedScrollConnection),
        ) {
            QuotaBanner(quota)
            FloatingHeader(filtersScrollBehavior, Modifier.testTag(FILTER_CHIPS_TAG)) {
                ActiveFilters(filtersState, onOpenFilters, onClearGenre, onClearRegion)
            }
            PagedPodcastGrid(
                podcasts = podcasts,
                favoriteIds = favoriteIds,
                emptyMessage = stringResource(Res.string.home_empty),
                onPodcastClick = onPodcastClick,
                onToggleFavorite = onToggleFavorite,
                onShare = onShare,
                gridTestTag = PODCASTS_LIST_TAG,
                emptyAction = onOpenDownloads?.let {
                    MessageAction(stringResource(Res.string.home_go_to_downloads), it)
                },
            )
        }
    }
}
