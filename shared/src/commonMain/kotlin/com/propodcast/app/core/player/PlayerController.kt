package com.propodcast.app.core.player

import com.propodcast.app.core.model.NowPlaying
import kotlinx.coroutines.flow.StateFlow

/** App-wide playback control (Media3 on Android, AVPlayer on iOS: stage 7). Call from the main thread. */
interface PlayerController {
    val state: StateFlow<PlayerState>

    /** Loads [item] and starts playing it from [startPositionMs]. */
    fun play(item: NowPlaying, startPositionMs: Long = 0)
    fun togglePlayPause()
    fun seekTo(positionMs: Long)
    fun seekBack()
    fun seekForward()
    fun setSpeed(speed: Float)

    /** Stops playback and forgets the episode (also the saved "continue" episode). */
    fun stop()

    companion object {
        const val SEEK_BACK_MS = 15_000L
        const val SEEK_FORWARD_MS = 30_000L
    }
}
