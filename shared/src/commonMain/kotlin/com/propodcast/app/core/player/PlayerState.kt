package com.propodcast.app.core.player

import com.propodcast.app.core.model.NowPlaying

/**
 * Snapshot of the player. [isPlaying] means "playing or about to" (also while buffering), so play / pause
 * buttons follow what the user asked for. [durationMs] is 0 while unknown.
 */
data class PlayerState(
    val nowPlaying: NowPlaying? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f,
    val error: PlayerError? = null,
)
