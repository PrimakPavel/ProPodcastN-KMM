package com.propodcast.app.widget

import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.toNowPlaying
import com.propodcast.app.core.player.PlayerController
import kotlinx.coroutines.flow.first

/** The widget's play / pause. With nothing loaded it continues the saved episode from its saved position. */
suspend fun toggleWidgetPlayback(controller: PlayerController, preferences: UserPreferencesRepository) {
    if (controller.state.value.nowPlaying != null) {
        controller.togglePlayPause()
    } else {
        preferences.lastPlayed.first()?.let { last -> controller.play(last.toNowPlaying(), last.positionMs) }
    }
}
