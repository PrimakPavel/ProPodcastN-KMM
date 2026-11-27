package com.propodcast.app.feature.settings.widgets

import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** One option of a single-choice setting; the whole row selects it. Put the rows in a `selectableGroup()`. */
@Composable
internal fun SettingRadioRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}
