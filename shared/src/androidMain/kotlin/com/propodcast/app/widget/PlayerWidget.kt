package com.propodcast.app.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.player.PlayerController
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Home screen widget with the current or last episode. The system creates it: dependencies come from Koin. */
class PlayerWidget : GlanceAppWidget(), KoinComponent {

    private val controller: PlayerController by inject()
    private val preferences: UserPreferencesRepository by inject()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val initial = playerWidgetState(controller.state.value, preferences.lastPlayed.first())
        val strings = loadPlayerWidgetStrings()

        provideContent {
            // While the session lives it follows the player itself; PlayerWidgetUpdater restarts it otherwise.
            val stateFlow = remember {
                combine(controller.state, preferences.lastPlayed, ::playerWidgetState).distinctUntilChanged()
            }
            val state by stateFlow.collectAsState(initial)
            val artwork by produceState<Bitmap?>(initialValue = null, state?.imageUrl) {
                value = state?.imageUrl?.let { loadWidgetArtwork(context, it) }
            }
            GlanceTheme {
                PlayerWidgetContent(state, artwork, strings)
            }
        }
    }
}

class PlayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PlayerWidget()
}
