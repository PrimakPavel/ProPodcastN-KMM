package com.propodcast.app.feature.settings

import com.propodcast.app.core.model.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val downloadOnlyOnWifi: Boolean = false,
)
