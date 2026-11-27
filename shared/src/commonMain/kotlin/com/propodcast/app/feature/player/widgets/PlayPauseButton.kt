package com.propodcast.app.feature.player.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_pause
import com.propodcast.app.resources.player_pause
import com.propodcast.app.resources.player_play
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp

/** Play / pause toggle; a spinner surrounds it while the audio is buffering. [large] is for the full player. */
@Composable
internal fun PlayPauseButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val painter = if (isPlaying) painterResource(Res.drawable.ic_pause) else rememberVectorPainter(Icons.Filled.PlayArrow)
    val description = stringResource(if (isPlaying) Res.string.player_pause else Res.string.player_play)
    val buttonSize = if (large) 72.dp else 48.dp
    Box(modifier.size(buttonSize), contentAlignment = Alignment.Center) {
        if (large) {
            FilledIconButton(onClick = onClick, modifier = Modifier.size(buttonSize)) {
                Icon(painter, contentDescription = description, modifier = Modifier.size(40.dp))
            }
        } else {
            IconButton(onClick = onClick) {
                Icon(painter, contentDescription = description)
            }
        }
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(if (large) buttonSize else 36.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}
