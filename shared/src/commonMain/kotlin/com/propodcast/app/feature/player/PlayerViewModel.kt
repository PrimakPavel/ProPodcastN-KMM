package com.propodcast.app.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.toNowPlaying
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.core.player.PlayerState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Activity-wide player UI: the live player, or the episode to continue from the last session. */
class PlayerViewModel(
    private val controller: PlayerController,
    preferences: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = combine(controller.state, preferences.lastPlayed) { playback, last ->
        when {
            playback.nowPlaying != null -> PlayerUiState(playback)
            last != null -> PlayerUiState(
                playback = PlayerState(
                    nowPlaying = last.toNowPlaying(),
                    positionMs = last.positionMs,
                    durationMs = last.durationMs,
                    speed = playback.speed,
                ),
                isRestored = true,
            )
            else -> PlayerUiState()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PlayerUiState())

    /**
     * Plays [episode]; the episode already in the player is paused / resumed instead of restarted.
     * Returns false when the episode has no audio file.
     */
    fun play(episode: Episode): Boolean {
        val item = episode.toNowPlaying() ?: return false
        val state = uiState.value
        val current = state.playback.nowPlaying
        when {
            current?.episodeId != item.episodeId -> controller.play(item)
            state.isRestored -> controller.play(item, state.playback.positionMs)
            else -> controller.togglePlayPause()
        }
        return true
    }

    fun togglePlayPause() {
        val state = uiState.value
        if (state.isRestored) loadRestored(state.playback.positionMs) else controller.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        if (uiState.value.isRestored) loadRestored(positionMs) else controller.seekTo(positionMs)
    }

    fun seekBack() {
        val state = uiState.value
        if (state.isRestored) {
            loadRestored((state.playback.positionMs - PlayerController.SEEK_BACK_MS).coerceAtLeast(0))
        } else {
            controller.seekBack()
        }
    }

    fun seekForward() {
        val state = uiState.value
        if (state.isRestored) {
            loadRestored(state.playback.positionMs + PlayerController.SEEK_FORWARD_MS)
        } else {
            controller.seekForward()
        }
    }

    fun setSpeed(speed: Float) = controller.setSpeed(speed)

    fun stop() = controller.stop()

    private fun loadRestored(positionMs: Long) {
        uiState.value.playback.nowPlaying?.let { controller.play(it, positionMs) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
