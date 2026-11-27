package com.propodcast.app.feature.player.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_continue
import com.propodcast.app.resources.player_error_network
import com.propodcast.app.resources.player_error_source
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.player.PlayerError
import com.propodcast.app.feature.player.PlayerUiState

/** Line under the title: the error, "Continue listening" for a restored episode, or the podcast name. */
@Composable
internal fun playerSubtitle(state: PlayerUiState): String? = when {
    state.playback.error == PlayerError.NETWORK -> stringResource(Res.string.player_error_network)
    state.playback.error == PlayerError.SOURCE -> stringResource(Res.string.player_error_source)
    state.isRestored -> stringResource(Res.string.player_continue)
    else -> state.playback.nowPlaying?.podcastTitle
}
