package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.filters_load_error
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import com.propodcast.app.core.ui.OptionsState
import com.propodcast.app.core.ui.message

/** Loading / error with retry / loaded list of filter options. */
@Composable
fun <T> OptionsList(
    options: OptionsState<T>,
    onRetry: () -> Unit,
    modifier: Modifier,
    content: LazyListScope.(List<T>) -> Unit,
) {
    when (options) {
        OptionsState.Loading -> LoadingState(modifier)
        is OptionsState.Error -> MessageState(
            message = stringResource(Res.string.filters_load_error) + "\n" + options.error.message(),
            onRetry = onRetry,
            modifier = modifier,
        )
        is OptionsState.Loaded -> LazyColumn(modifier) { content(options.items) }
    }
}
