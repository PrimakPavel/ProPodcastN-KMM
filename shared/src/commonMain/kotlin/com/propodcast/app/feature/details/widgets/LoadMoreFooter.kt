package com.propodcast.app.feature.details.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.details_load_more
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.ListFooterError
import com.propodcast.app.core.ui.message
import com.propodcast.app.feature.details.DetailsContent

/** Load more button, spinner while loading, or a retryable error. Nothing when all episodes are loaded. */
internal fun LazyListScope.loadMoreFooter(content: DetailsContent.Loaded, onLoadMore: () -> Unit) {
    when {
        content.isLoadingMore -> item(key = "load-more") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        content.loadMoreError != null -> item(key = "load-more") {
            ListFooterError(message = content.loadMoreError.message(), onRetry = onLoadMore)
        }
        content.canLoadMore -> item(key = "load-more") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                OutlinedButton(onClick = onLoadMore) { Text(stringResource(Res.string.details_load_more)) }
            }
        }
    }
}
