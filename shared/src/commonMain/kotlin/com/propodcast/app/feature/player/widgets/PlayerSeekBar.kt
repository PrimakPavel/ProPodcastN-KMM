package com.propodcast.app.feature.player.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.ui.formatPlaybackTime
import com.propodcast.app.feature.player.PLAYER_SEEK_BAR_TAG

/** Slider with elapsed and remaining time. Seeks once the thumb is released, not on every move. */
@Composable
internal fun PlayerSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val known = durationMs > 0
    val fraction = dragFraction ?: if (known) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val shownPositionMs = if (dragFraction != null) (fraction * durationMs).toLong() else positionMs
    val currentOnSeek by rememberUpdatedState(onSeek)
    val currentDurationMs by rememberUpdatedState(durationMs)

    Column(modifier.fillMaxWidth()) {
        Slider(
            // While the user drags, the thumb follows the finger; otherwise it follows playback.
            value = dragFraction ?: fraction,
            // The player seeks on release.
            onValueChange = { value -> dragFraction = value },
            onValueChangeFinished = {
                dragFraction?.let { currentOnSeek((it * currentDurationMs).toLong()) }
                dragFraction = null
            },
            enabled = known,
            modifier = Modifier.testTag(PLAYER_SEEK_BAR_TAG),
            // Only a hairline gap between the thumb and the track (the default is 6 dp).
            track = { state ->
                SliderDefaults.Track(sliderState = state, enabled = known, thumbTrackGapSize = 1.dp)
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = formatPlaybackTime(shownPositionMs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (known) {
                Text(
                    text = "-" + formatPlaybackTime(durationMs - shownPositionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
