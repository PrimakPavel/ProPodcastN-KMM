package com.propodcast.app.core.ui

import org.jetbrains.compose.resources.StringResource

/**
 * A snackbar a ViewModel asks the screen to show (see [SnackbarEffect]); the shared shape of every
 * "Removed · Undo" / "Cancelled · Retry" message. [onAction] runs when its action is tapped, [onDismiss]
 * when it goes away otherwise (timeout, swipe, ✕ or replaced by a newer snackbar).
 */
class SnackbarEvent(
    val messageRes: StringResource,
    val messageArgs: List<Any> = emptyList(),
    val actionLabelRes: StringResource? = null,
    val onAction: () -> Unit = {},
    val onDismiss: () -> Unit = {},
)
