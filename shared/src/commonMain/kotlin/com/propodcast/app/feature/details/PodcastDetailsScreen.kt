package com.propodcast.app.feature.details

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_no_audio
import com.propodcast.app.resources.share_chooser_title
import com.propodcast.app.resources.share_episode_chooser_title
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.Podcast
import com.propodcast.app.core.ui.rememberLinkSharer
import com.propodcast.app.core.ui.SnackbarEffect
import com.propodcast.app.feature.downloads.DownloadsViewModel
import com.propodcast.app.feature.downloads.CellularDownloadPrompt
import com.propodcast.app.feature.downloads.rememberEpisodeDownloadActions
import kotlinx.coroutines.launch

@Composable
fun PodcastDetailsScreen(
    onBack: () -> Unit,
    onPodcastClick: (Podcast) -> Unit,
    onPlayEpisode: (Episode) -> Boolean,
    modifier: Modifier = Modifier,
    preview: DetailsPreview = DetailsPreview(),
    viewModel: PodcastDetailsViewModel = koinViewModel(),
    downloadsViewModel: DownloadsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloads = rememberEpisodeDownloadActions(downloadsViewModel)
    val sharer = rememberLinkSharer()
    val sharePodcastTitle = stringResource(Res.string.share_chooser_title)
    val shareEpisodeTitle = stringResource(Res.string.share_episode_chooser_title)
    val noAudio = stringResource(Res.string.player_no_audio)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    SnackbarEffect(downloadsViewModel.snackbarEvents, snackbarHostState)
    CellularDownloadPrompt(downloadsViewModel)

    PodcastDetailsContent(
        state = state,
        downloads = downloads,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::load,
        onLoadMore = viewModel::loadMoreEpisodes,
        onTogglePodcastFavorite = viewModel::togglePodcastFavorite,
        onSharePodcast = { podcast ->
            podcast.listennotesUrl?.let { sharer.share(podcast.title, it, sharePodcastTitle) }
        },
        onEpisodeClick = { episode ->
            if (!onPlayEpisode(episode)) scope.launch { snackbarHostState.showSnackbar(noAudio) }
        },
        onToggleEpisodeFavorite = viewModel::toggleEpisodeFavorite,
        onShareEpisode = { episode ->
            episode.listennotesUrl?.let { sharer.share(episode.title, it, shareEpisodeTitle) }
        },
        onPodcastClick = onPodcastClick,
        modifier = modifier,
        preview = preview,
    )
}
