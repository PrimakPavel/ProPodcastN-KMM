package com.propodcast.app.feature.player.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.player.PlayerState

/** Seek bar, -15 / play-pause / +30, then speed and (when [onDownloadClick] is set) download. */
@Composable
internal fun PlayerControls(
    playback: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    downloadState: EpisodeDownloadState?,
    onDownloadClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        PlayerSeekBar(positionMs = playback.positionMs, durationMs = playback.durationMs, onSeek = onSeekTo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkipButton(seconds = (PlayerController.SEEK_BACK_MS / 1_000).toInt(), forward = false, onClick = onSeekBack)
            PlayPauseButton(
                isPlaying = playback.isPlaying,
                isBuffering = playback.isBuffering,
                onClick = onTogglePlayPause,
                large = true,
            )
            SkipButton(seconds = (PlayerController.SEEK_FORWARD_MS / 1_000).toInt(), forward = true, onClick = onSeekForward)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpeedButton(speed = playback.speed, onSpeedSelected = onSpeedSelected)
            if (onDownloadClick != null) DownloadButton(state = downloadState, onClick = onDownloadClick)
        }
    }
}
