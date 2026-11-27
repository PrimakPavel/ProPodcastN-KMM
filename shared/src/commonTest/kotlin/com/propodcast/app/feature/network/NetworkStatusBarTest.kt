package com.propodcast.app.feature.network

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.runComposeUiTest
import com.propodcast.app.core.testing.UiTestBase
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.propodcast.app.core.designsystem.theme.ProPodcastTheme
import kotlin.test.Test

class NetworkStatusBarTest : UiTestBase() {

    @Test
    fun followsTheBannerState() = runComposeUiTest {
        var banner by mutableStateOf(NetworkBanner.HIDDEN)
        setContent { ProPodcastTheme { NetworkStatusBar(banner) } }

        onNodeWithTag(NETWORK_STATUS_BAR_TAG).assertDoesNotExist()

        banner = NetworkBanner.OFFLINE
        onNodeWithText("No connection").assertIsDisplayed()

        banner = NetworkBanner.BACK_ONLINE
        onNodeWithText("Back online").assertIsDisplayed()

        banner = NetworkBanner.HIDDEN
        waitForIdle()
        onNodeWithTag(NETWORK_STATUS_BAR_TAG).assertDoesNotExist()
    }
}
