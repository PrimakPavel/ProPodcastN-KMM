package com.propodcast.app.widget

import android.app.Application
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.testing.unit.hasRunCallbackClickAction
import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasContentDescriptionEqualTo
import androidx.glance.testing.unit.hasText
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric: Glance needs a real Context. A plain Application keeps Hilt and the player out of it.
 * SDK 34: Robolectric's sandboxes for SDK 35+ need Java 21 and the build runs on Java 17; the widget
 * uses nothing newer than API 31.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PlayerWidgetContentTest {

    private val strings = PlayerWidgetStrings(
        empty = "Nothing played yet. Tap to find a podcast.",
        sourceError = "This episode can’t be played.",
        play = "Play",
        pause = "Pause",
    )

    private val state = PlayerWidgetState(
        episodeTitle = "Episode one",
        podcastTitle = "The Podcast",
        imageUrl = null,
        isPlaying = true,
    )

    @Test
    fun showsEpisode_andPauseRunsTheToggleAction() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        setAppWidgetSize(DpSize(250.dp, 64.dp))
        provideComposable { PlayerWidgetContent(state, artwork = null, strings) }

        onNode(hasText("Episode one")).assertExists()
        onNode(hasText("The Podcast")).assertExists()
        onNode(hasContentDescriptionEqualTo("Pause")).assertExists()
        // The button runs the action; its icon carries the description.
        onNode(hasRunCallbackClickAction<TogglePlaybackAction>()).assertExists()
    }

    @Test
    fun pausedEpisode_offersPlay() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        provideComposable { PlayerWidgetContent(state.copy(isPlaying = false), artwork = null, strings) }

        onNode(hasContentDescriptionEqualTo("Play")).assertExists()
    }

    @Test
    fun error_replacesPodcastTitle() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        provideComposable { PlayerWidgetContent(state.copy(hasError = true), artwork = null, strings) }

        onNode(hasText("This episode can’t be played.")).assertExists()
        onNode(hasText("The Podcast")).assertDoesNotExist()
    }

    @Test
    fun nothingPlayed_showsHint() = runGlanceAppWidgetUnitTest {
        setContext(ApplicationProvider.getApplicationContext())
        provideComposable { PlayerWidgetContent(state = null, artwork = null, strings = strings) }

        onNode(hasText("Nothing played yet. Tap to find a podcast.")).assertExists()
    }
}
