package com.propodcast.app.core.testing

import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.player.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow

/** Records calls; tests drive [state] directly. */
class FakePlayerController : PlayerController {
    override val state = MutableStateFlow(PlayerState())
    val calls = mutableListOf<String>()

    override fun play(item: NowPlaying, startPositionMs: Long) {
        calls += "play ${item.episodeId} @$startPositionMs"
        state.value = PlayerState(nowPlaying = item, isPlaying = true, positionMs = startPositionMs)
    }

    override fun togglePlayPause() {
        calls += "toggle"
    }

    override fun seekTo(positionMs: Long) {
        calls += "seekTo $positionMs"
    }

    override fun seekBack() {
        calls += "back"
    }

    override fun seekForward() {
        calls += "forward"
    }

    override fun setSpeed(speed: Float) {
        calls += "speed $speed"
        state.value = state.value.copy(speed = speed)
    }

    override fun stop() {
        calls += "stop"
        state.value = PlayerState()
    }
}
