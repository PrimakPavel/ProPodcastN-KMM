package com.propodcast.app.feature.settings

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.settings_section_downloads
import com.propodcast.app.resources.settings_section_theme
import com.propodcast.app.resources.settings_theme_dark
import com.propodcast.app.resources.settings_theme_light
import com.propodcast.app.resources.settings_theme_system
import com.propodcast.app.resources.settings_title
import com.propodcast.app.resources.settings_wifi_only
import com.propodcast.app.resources.settings_wifi_only_summary
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.commonwidgets.BackButton
import com.propodcast.app.core.model.ThemeMode
import com.propodcast.app.feature.settings.widgets.SettingRadioRow
import com.propodcast.app.feature.settings.widgets.SettingSwitchRow
import com.propodcast.app.feature.settings.widgets.SettingsSectionTitle

/** Stateless settings, also used by UI tests. */
@Composable
fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDownloadOnlyOnWifiChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.settings_title)) },
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsSectionTitle(stringResource(Res.string.settings_section_theme))
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    SettingRadioRow(
                        title = stringResource(mode.labelRes),
                        selected = state.themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
            SettingsSectionTitle(stringResource(Res.string.settings_section_downloads))
            SettingSwitchRow(
                title = stringResource(Res.string.settings_wifi_only),
                summary = stringResource(Res.string.settings_wifi_only_summary),
                checked = state.downloadOnlyOnWifi,
                onCheckedChange = onDownloadOnlyOnWifiChange,
            )
        }
    }
}

private val ThemeMode.labelRes: StringResource
    get() = when (this) {
        ThemeMode.SYSTEM -> Res.string.settings_theme_system
        ThemeMode.LIGHT -> Res.string.settings_theme_light
        ThemeMode.DARK -> Res.string.settings_theme_dark
    }
