package com.propodcast.app.core.player

import com.propodcast.app.core.model.NowPlaying
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Temporary [PlayerController] until the platform players (stage 7): keeps the state the UI shows
 * (episode, play / pause, position, speed) but plays no sound.
 */
class StubPlayerController : PlayerController {
    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    override fun play(item: NowPlaying, startPositionMs: Long) {
        _state.value = PlayerState(nowPlaying = item, isPlaying = true, positionMs = startPositionMs, durationMs = item.durationMs)
    }

    override fun togglePlayPause() = _state.update { it.copy(isPlaying = !it.isPlaying && it.nowPlaying != null) }

    override fun seekTo(positionMs: Long) = _state.update { it.copy(positionMs = positionMs.coerceIn(0, it.durationMs.coerceAtLeast(0))) }

    override fun seekBack() = seekTo(_state.value.positionMs - PlayerController.SEEK_BACK_MS)

    override fun seekForward() = seekTo(_state.value.positionMs + PlayerController.SEEK_FORWARD_MS)

    override fun setSpeed(speed: Float) = _state.update { it.copy(speed = speed) }

    override fun stop() {
        _state.value = PlayerState()
    }
}
