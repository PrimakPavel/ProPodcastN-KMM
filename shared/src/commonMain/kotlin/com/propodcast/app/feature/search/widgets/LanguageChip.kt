package com.propodcast.app.feature.search.widgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.search_language_any
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp

/** Shows the search language ("Any language" when none) and opens the language sheet. */
@Composable
internal fun LanguageChip(
    language: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = language != null,
        onClick = onClick,
        label = { Text(language ?: stringResource(Res.string.search_language_any)) },
        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        modifier = modifier.padding(horizontal = 16.dp),
    )
}
