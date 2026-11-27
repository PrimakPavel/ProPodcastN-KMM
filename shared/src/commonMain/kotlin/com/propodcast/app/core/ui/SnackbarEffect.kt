package com.propodcast.app.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

/**
 * Shows [events] on [snackbarHostState]. A newer snackbar replaces the shown one (whose [SnackbarEvent.onDismiss]
 * then runs) instead of waiting in the queue. Snackbars with an action stay long enough to reach it.
 */
@Composable
fun SnackbarEffect(events: Flow<SnackbarEvent>, snackbarHostState: SnackbarHostState) {
    LaunchedEffect(events, snackbarHostState) {
        events.collect { event ->
            snackbarHostState.currentSnackbarData?.dismiss()
            launch {
                val result = snackbarHostState.showSnackbar(
                    message = getString(event.messageRes, *event.messageArgs.toTypedArray()),
                    actionLabel = event.actionLabelRes?.let { getString(it) },
                    withDismissAction = true,
                    duration = if (event.actionLabelRes != null) SnackbarDuration.Long else SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) event.onAction() else event.onDismiss()
            }
        }
    }
}
