package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_back
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

/** Top app bar "up" button. */
@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
    }
}
