package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Shared element key of a podcast's artwork shown on the [origin] screen. */
fun podcastArtworkKey(origin: String, podcastId: String): String = "podcast-artwork/$origin/$podcastId"

/**
 * Makes this artwork a shared element ("hero") with [key] inside the current navigation transition.
 * A no-op without a key or outside the app's shared transition scopes (previews, tests).
 */
@Composable
fun Modifier.sharedArtwork(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val visibilityScope = LocalNavAnimatedVisibilityScope.current
    if (key == null || sharedScope == null || visibilityScope == null) return this
    return with(sharedScope) {
        this@sharedArtwork.sharedElement(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
        )
    }
}

/** [sharedArtwork] for a podcast in a list, keyed by the current [LocalArtworkOrigin]. */
@Composable
fun Modifier.sharedPodcastArtwork(podcastId: String): Modifier =
    sharedArtwork(LocalArtworkOrigin.current?.let { podcastArtworkKey(it, podcastId) })
