package com.propodcast.app.feature.player

import com.propodcast.app.core.player.PlayerState

/**
 * What the mini and full player show. When [isRestored], [playback] holds the episode saved from an earlier
 * session: it is not loaded into the player yet, and play / seek load it from the saved position.
 */
data class PlayerUiState(
    val playback: PlayerState = PlayerState(),
    val isRestored: Boolean = false,
) {
    val isVisible: Boolean
        get() = playback.nowPlaying != null

    /** 0..1, or null while the duration is unknown. */
    val progress: Float?
        get() = playback.durationMs.takeIf { it > 0 }
            ?.let { (playback.positionMs.toFloat() / it).coerceIn(0f, 1f) }
}
