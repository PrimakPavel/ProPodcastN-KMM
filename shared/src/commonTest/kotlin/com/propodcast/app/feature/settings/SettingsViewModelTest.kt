package com.propodcast.app.feature.settings

import com.propodcast.app.core.model.ThemeMode
import com.propodcast.app.core.testing.FakeUserPreferencesRepository
import com.propodcast.app.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SettingsViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    @BeforeTest
    fun setMainDispatcher() = mainDispatcherRule.start()

    @AfterTest
    fun resetMainDispatcher() = mainDispatcherRule.finish()

    private val preferences = FakeUserPreferencesRepository()

    @Test
    fun wifiOnly_followsAndUpdatesTheSetting() = runTest {
        val viewModel = SettingsViewModel(preferences)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertFalse(viewModel.uiState.value.downloadOnlyOnWifi)

        viewModel.setDownloadOnlyOnWifi(true)

        assertTrue(preferences.downloadOnlyOnWifi.value)
        assertTrue(viewModel.uiState.value.downloadOnlyOnWifi)
    }

    @Test
    fun themeMode_followsAndUpdatesTheSetting() = runTest {
        val viewModel = SettingsViewModel(preferences)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertEquals(ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)

        viewModel.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, preferences.themeMode.value)
        assertEquals(ThemeMode.DARK, viewModel.uiState.value.themeMode)
    }
}
