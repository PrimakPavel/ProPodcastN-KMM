package com.propodcast.app.feature.network

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.network_back_online
import com.propodcast.app.resources.network_offline
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Thin strip at the bottom: red "No connection" while offline, green "Back online" briefly after.
 * Slides in and out; TalkBack announces the change.
 */
@Composable
fun NetworkStatusBar(banner: NetworkBanner, modifier: Modifier = Modifier) {
    // Keep the last visible state while the strip slides away, so its text doesn't vanish first.
    var shown by remember { mutableStateOf(NetworkBanner.OFFLINE) }
    if (banner != NetworkBanner.HIDDEN) shown = banner
    val color by animateColorAsState(
        targetValue = if (shown == NetworkBanner.OFFLINE) OFFLINE_RED else BACK_ONLINE_GREEN,
        label = "networkBarColor",
    )
    AnimatedVisibility(
        visible = banner != NetworkBanner.HIDDEN,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(vertical = 1.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
                .testTag(NETWORK_STATUS_BAR_TAG),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(
                    if (shown == NetworkBanner.OFFLINE) Res.string.network_offline else Res.string.network_back_online,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
            )
        }
    }
}

const val NETWORK_STATUS_BAR_TAG = "network_status_bar"

// Fixed colours, the same in light and dark theme (the theme's error colour turns pale pink in dark mode).
private val OFFLINE_RED = Color(0xFFC62828)
private val BACK_ONLINE_GREEN = Color(0xFF2E7D32)
