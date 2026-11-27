package com.propodcast.app.feature.search.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_clear_search
import com.propodcast.app.resources.search_placeholder
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.propodcast.app.feature.search.SEARCH_FIELD_TAG

/**
 * Single-line search input with a clear button. Gets focus when opened with an empty query;
 * the keyboard's Search action just hides the keyboard (results already update while typing).
 */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (query.isEmpty()) focusRequester.requestFocus()
    }
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(Res.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(Res.string.action_clear_search))
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .focusRequester(focusRequester)
            .testTag(SEARCH_FIELD_TAG),
    )
}
