package com.propodcast.app.feature.player.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.propodcast.app.core.commonwidgets.PodcastImage
import com.propodcast.app.feature.player.PLAYER_ARTWORK_KEY

/**
 * Mini player artwork. With a [sharedTransitionScope] it is the start / end of the artwork flight to the full
 * player: hidden while [expanded], with the box keeping the row's layout meanwhile.
 */
@Composable
internal fun MiniPlayerArtwork(
    imageUrl: String?,
    size: Dp,
    expanded: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
) {
    Box(Modifier.size(size)) {
        if (sharedTransitionScope == null) {
            PodcastImage(url = imageUrl, modifier = Modifier.size(size))
            return@Box
        }
        AnimatedVisibility(visible = !expanded, enter = fadeIn(), exit = fadeOut()) {
            val shared = with(sharedTransitionScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(PLAYER_ARTWORK_KEY),
                    animatedVisibilityScope = this@AnimatedVisibility,
                )
            }
            PodcastImage(url = imageUrl, modifier = shared.size(size))
        }
    }
}
