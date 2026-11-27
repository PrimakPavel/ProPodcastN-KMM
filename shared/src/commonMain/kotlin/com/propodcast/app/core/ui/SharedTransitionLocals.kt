package com.propodcast.app.core.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.staticCompositionLocalOf

/** The app-wide [SharedTransitionScope] (ProPodcastApp); null in previews and tests, which disables the effect. */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The enter / exit animation scope of the current navigation destination (AppNavHost). */
val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Which screen shows the podcast artwork ("home", "search", …). It is part of the shared element key so a
 * podcast shown on two tabs does not fly between them on a tab switch, only to and from its details.
 */
val LocalArtworkOrigin = staticCompositionLocalOf<String?> { null }
