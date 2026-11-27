package com.propodcast.app.feature.search

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.search_empty
import com.propodcast.app.resources.search_min_length
import com.propodcast.app.resources.search_start
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import org.jetbrains.compose.resources.stringResource
import androidx.paging.compose.LazyPagingItems
import com.propodcast.app.core.commonwidgets.FloatingHeader
import com.propodcast.app.core.commonwidgets.MessageState
import com.propodcast.app.core.commonwidgets.PagedPodcastGrid
import com.propodcast.app.core.commonwidgets.rememberFloatingHeaderScrollBehavior
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.feature.search.widgets.LanguageChip
import com.propodcast.app.feature.search.widgets.SearchField

/** Stateless search content, also used by UI tests. */
@Composable
fun SearchContent(
    query: String,
    language: String?,
    results: LazyPagingItems<Podcast>,
    favoriteIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onOpenLanguages: () -> Unit,
    onPodcastClick: (Podcast) -> Unit,
    onToggleFavorite: (Podcast) -> Unit,
    onShare: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The language chip floats like the home filter chips; scrolling also hides the keyboard.
    val languageScrollBehavior = rememberFloatingHeaderScrollBehavior()
    val keyboard = LocalSoftwareKeyboardController.current
    val hideKeyboardOnScroll = remember(keyboard) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) keyboard?.hide()
                return Offset.Zero
            }
        }
    }
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(languageScrollBehavior.nestedScrollConnection)
                .nestedScroll(hideKeyboardOnScroll),
        ) {
            SearchField(query = query, onQueryChange = onQueryChange)
            FloatingHeader(languageScrollBehavior) {
                LanguageChip(language = language, onClick = onOpenLanguages)
            }
            // Hints and results cross-fade with a small vertical slide when the mode changes.
            AnimatedContent(
                targetState = SearchMode.of(query),
                transitionSpec = {
                    (fadeIn(tween(HINT_ANIMATION_MS)) + slideInVertically(tween(HINT_ANIMATION_MS)) { it / 12 })
                        .togetherWith(fadeOut(tween(HINT_ANIMATION_MS / 2)))
                },
                label = "searchMode",
            ) { mode ->
                when (mode) {
                    SearchMode.START -> MessageState(message = stringResource(Res.string.search_start))
                    SearchMode.TOO_SHORT -> MessageState(
                        message = stringResource(Res.string.search_min_length, SearchViewModel.MIN_QUERY_LENGTH),
                    )
                    SearchMode.RESULTS -> PagedPodcastGrid(
                        podcasts = results,
                        favoriteIds = favoriteIds,
                        emptyMessage = stringResource(Res.string.search_empty, query.trim()),
                        onPodcastClick = onPodcastClick,
                        onToggleFavorite = onToggleFavorite,
                        onShare = onShare,
                        gridTestTag = SEARCH_RESULTS_TAG,
                    )
                }
            }
        }
    }
}

private const val HINT_ANIMATION_MS = 250
