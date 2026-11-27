package com.propodcast.app.feature.search.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.search_language_any
import com.propodcast.app.resources.search_language_title
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.OptionRow
import com.propodcast.app.core.commonwidgets.OptionsList
import com.propodcast.app.core.commonwidgets.SheetHeader
import com.propodcast.app.core.commonwidgets.rememberSheetCloser
import com.propodcast.app.feature.search.LanguageFilterState

/** Bottom sheet to pick the search language; picking one applies it and closes the sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LanguageSheet(
    state: LanguageFilterState,
    onSelect: (String?) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val close = rememberSheetCloser(sheetState, onDismiss)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column {
            SheetHeader(title = stringResource(Res.string.search_language_title), onClose = close)
            OptionsList(
                options = state.languages,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp),
            ) { languages ->
                item(key = "any") {
                    OptionRow(stringResource(Res.string.search_language_any), selected = state.selected == null) {
                        onSelect(null)
                    }
                }
                items(languages, key = { it }) { language ->
                    OptionRow(language, selected = language == state.selected) { onSelect(language) }
                }
            }
        }
    }
}
