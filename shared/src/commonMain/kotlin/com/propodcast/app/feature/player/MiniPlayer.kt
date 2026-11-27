package com.propodcast.app.feature.player

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_close
import com.propodcast.app.resources.player_open
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.propodcast.app.feature.player.widgets.MiniPlayerArtwork
import com.propodcast.app.feature.player.widgets.PlayPauseButton
import com.propodcast.app.feature.player.widgets.playerSubtitle

/**
 * Compact player above the navigation bar; a tap opens the full player. Draws nothing without an episode.
 * With a [sharedTransitionScope], the artwork flies into the full player while [expanded].
 */
@Composable
fun MiniPlayer(
    state: PlayerUiState,
    onOpen: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val item = state.playback.nowPlaying ?: return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(MINI_PLAYER_TAG),
        tonalElevation = 3.dp,
    ) {
        Column {
            val progress = state.progress
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            } else {
                Spacer(Modifier.height(2.dp))
            }
            Row(
                modifier = Modifier
                    .clickable(onClickLabel = stringResource(Res.string.player_open), onClick = onOpen)
                    .padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MiniPlayerArtwork(
                    imageUrl = item.imageUrl,
                    size = ARTWORK_SIZE,
                    expanded = expanded,
                    sharedTransitionScope = sharedTransitionScope,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    playerSubtitle(state)?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.playback.error != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                PlayPauseButton(
                    isPlaying = state.playback.isPlaying,
                    isBuffering = state.playback.isBuffering,
                    onClick = onTogglePlayPause,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.player_close))
                }
            }
        }
    }
}

private val ARTWORK_SIZE = 44.dp
