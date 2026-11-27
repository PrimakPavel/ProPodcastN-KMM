package com.propodcast.app.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import kotlin.test.Test

class SettingsContentTest : UiTestBase() {

    @Test
    fun wifiOnlyRow_togglesTheSwitch() = runComposeUiTest {
        var state by mutableStateOf(SettingsUiState())
        setContent {
            ProPodcastTheme {
                SettingsContent(
                    state = state,
                    onBack = {},
                    onThemeModeChange = { state = state.copy(themeMode = it) },
                    onDownloadOnlyOnWifiChange = { state = state.copy(downloadOnlyOnWifi = it) },
                )
            }
        }

        onNodeWithText("Download only on Wi-Fi").assertIsOff().performClick()
        onNodeWithText("Download only on Wi-Fi").assertIsOn()
    }

    @Test
    fun themeRows_selectOneMode() = runComposeUiTest {
        var state by mutableStateOf(SettingsUiState())
        setContent {
            ProPodcastTheme {
                SettingsContent(
                    state = state,
                    onBack = {},
                    onThemeModeChange = { state = state.copy(themeMode = it) },
                    onDownloadOnlyOnWifiChange = {},
                )
            }
        }

        onNodeWithText("Same as device").assertIsSelected()
        onNodeWithText("Dark").performClick().assertIsSelected()
        onNodeWithText("Same as device").assertIsNotSelected()
    }
}
