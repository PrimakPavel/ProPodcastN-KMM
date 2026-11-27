package com.propodcast.app.widget

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_error_source
import com.propodcast.app.resources.player_pause
import com.propodcast.app.resources.player_play
import com.propodcast.app.resources.widget_empty
import org.jetbrains.compose.resources.getString

/** The widget's texts, read from Compose Resources before Glance draws (Glance has no Compose Resources). */
data class PlayerWidgetStrings(
    val empty: String,
    val sourceError: String,
    val play: String,
    val pause: String,
)

internal suspend fun loadPlayerWidgetStrings() = PlayerWidgetStrings(
    empty = getString(Res.string.widget_empty),
    sourceError = getString(Res.string.player_error_source),
    play = getString(Res.string.player_play),
    pause = getString(Res.string.player_pause),
)
