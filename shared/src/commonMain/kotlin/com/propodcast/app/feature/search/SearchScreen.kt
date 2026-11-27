package com.propodcast.app.feature.search

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.share_chooser_title
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.rememberLinkSharer
import com.propodcast.app.feature.search.widgets.LanguageSheet

@Composable
fun SearchScreen(
    onPodcastClick: (Podcast) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val languageFilter by viewModel.languageFilter.collectAsStateWithLifecycle()
    val results = viewModel.results.collectAsLazyPagingItems()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()
    var showLanguages by rememberSaveable { mutableStateOf(false) }
    val sharer = rememberLinkSharer()
    val shareTitle = stringResource(Res.string.share_chooser_title)

    SearchContent(
        query = query,
        language = languageFilter.selected,
        results = results,
        favoriteIds = favoriteIds,
        onQueryChange = viewModel::onQueryChange,
        onOpenLanguages = { showLanguages = true },
        onPodcastClick = onPodcastClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onShare = { podcast -> podcast.listennotesUrl?.let { sharer.share(podcast.title, it, shareTitle) } },
        modifier = modifier,
    )

    if (showLanguages) {
        LanguageSheet(
            state = languageFilter,
            onSelect = { language ->
                viewModel.selectLanguage(language)
                showLanguages = false
            },
            onRetry = viewModel::loadLanguages,
            onDismiss = { showLanguages = false },
        )
    }
}
