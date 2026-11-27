package com.propodcast.app.core.ui

import com.propodcast.app.core.common.AppError

/** Loading state of a list of filter options (genres or regions). */
sealed interface OptionsState<out T> {
    data object Loading : OptionsState<Nothing>
    data class Error(val error: AppError) : OptionsState<Nothing>
    data class Loaded<T>(val items: List<T>) : OptionsState<T>
}
