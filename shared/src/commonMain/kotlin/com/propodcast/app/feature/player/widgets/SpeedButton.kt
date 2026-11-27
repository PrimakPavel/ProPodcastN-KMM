package com.propodcast.app.feature.player.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_speed
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.propodcast.app.core.player.PLAYBACK_SPEEDS
import com.propodcast.app.core.ui.formatSpeed
import kotlin.math.abs

/** Shows the current speed ("1.5×"); a tap opens a menu with all speeds, the current one checked. */
@Composable
internal fun SpeedButton(
    speed: Float,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = formatSpeed(speed)
    val description = stringResource(Res.string.player_speed, label)
    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.semantics { contentDescription = description },
        ) {
            Text(label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PLAYBACK_SPEEDS.forEach { option ->
                val selected = abs(option - speed) < SPEED_EPSILON
                DropdownMenuItem(
                    text = { Text(formatSpeed(option)) },
                    // Every row keeps the icon slot, so the labels line up.
                    leadingIcon = {
                        if (selected) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                    onClick = {
                        expanded = false
                        onSpeedSelected(option)
                    },
                )
            }
        }
    }
}

private const val SPEED_EPSILON = 0.01f
