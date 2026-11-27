package com.propodcast.app.feature.search

import com.propodcast.app.core.ui.OptionsState

/** Search language filter: the selected language (null = any) and the available ones. */
data class LanguageFilterState(
    val selected: String? = null,
    val languages: OptionsState<String> = OptionsState.Loading,
)
