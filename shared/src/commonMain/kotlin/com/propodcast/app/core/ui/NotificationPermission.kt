package com.propodcast.app.core.ui

import androidx.compose.runtime.Composable

/**
 * Returns `withNotificationPermission(action)`: on Android 13+ it first asks for POST_NOTIFICATIONS (once per
 * screen, the system limits repeats anyway), then runs `action` whatever the answer: features keep working
 * without the permission, only their notifications stay hidden. Elsewhere it just runs `action`.
 */
@Composable
expect fun rememberNotificationPermissionRequester(): (action: () -> Unit) -> Unit
