package com.propodcast.app.feature.filters

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import com.propodcast.app.core.model.Genre
import com.propodcast.app.core.ui.OptionsState
import kotlin.test.assertEquals
import kotlin.test.Test

class FiltersSheetTest : UiTestBase() {

    @Test
    fun closeButton_hidesSheetAndDismisses() = runComposeUiTest {
        var dismissed = 0
        setContent {
            ProPodcastTheme {
                FiltersSheet(
                    state = FiltersUiState(genres = OptionsState.Loaded(listOf(Genre(1, "History", null)))),
                    onSelectGenre = {},
                    onSelectRegion = {},
                    onRetryGenres = {},
                    onRetryRegions = {},
                    onDismiss = { dismissed++ },
                )
            }
        }

        onNodeWithText("Filters").assertIsDisplayed()
        onNodeWithContentDescription("Close").performClick()
        waitForIdle()

        assertEquals(1, dismissed)
    }
}
