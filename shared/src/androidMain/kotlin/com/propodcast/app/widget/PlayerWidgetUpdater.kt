package com.propodcast.app.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.player.PlayerController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Redraws the widget when what it shows changes (episode, play / pause, error). Glance only keeps a widget
 * session alive for a short while, so without this the widget would go stale while the app plays in background.
 */
class PlayerWidgetUpdater(
    private val context: Context,
    private val controller: PlayerController,
    private val preferences: UserPreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            combine(controller.state, preferences.lastPlayed, ::playerWidgetState)
                .distinctUntilChanged()
                .collect { PlayerWidget().updateAll(context) }
        }
    }
}
