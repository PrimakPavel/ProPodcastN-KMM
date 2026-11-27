package com.propodcast.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import org.koin.core.component.KoinComponent

/** Widget play / pause. With nothing loaded it continues the saved episode from its saved position. */
class TogglePlaybackAction : ActionCallback, KoinComponent {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        toggleWidgetPlayback(getKoin().get(), getKoin().get())
    }
}
