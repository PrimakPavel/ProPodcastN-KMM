package com.propodcast.app.core.commonwidgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.propodcast.app.core.ui.userMessage

/** Full-width spinner or retryable error after the last loaded page of a paged grid. */
fun <T : Any> LazyGridScope.pagingAppendFooter(items: LazyPagingItems<T>) {
    when (val append = items.loadState.append) {
        is LoadState.Loading -> item(key = "append-loading", span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is LoadState.Error -> item(key = "append-error", span = { GridItemSpan(maxLineSpan) }) {
            ListFooterError(message = append.error.userMessage(), onRetry = items::retry)
        }
        is LoadState.NotLoading -> Unit
    }
}
