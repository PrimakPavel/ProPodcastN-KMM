package com.propodcast.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(preferences.themeMode, preferences.downloadOnlyOnWifi) { themeMode, wifiOnly ->
            SettingsUiState(themeMode = themeMode, downloadOnlyOnWifi = wifiOnly)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setDownloadOnlyOnWifi(enabled: Boolean) {
        viewModelScope.launch { preferences.setDownloadOnlyOnWifi(enabled) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
