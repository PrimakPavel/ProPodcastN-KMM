package com.propodcast.app.feature.favorites.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Swipe in either direction to remove; the revealed background shows a delete icon on the swipe side.
 *
 * SwipeToDismissBox calls onDismiss whenever its state is settled as dismissed, including when a row enters
 * composition already dismissed. Undo brings the same item (same key) back, so the state must not outlive
 * the row: it is a plain `remember` (rememberSwipeToDismissBoxState is saveable, and LazyColumn restores
 * saved state by key), and lists must not keep removed rows alive in a fade-out (see [removableItemModifier]).
 */
@Composable
internal fun SwipeToRemove(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val state = remember { SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, positionalThreshold) }
    // Stable callback: onDismiss is a LaunchedEffect key, a new lambda each recomposition would call it again.
    val currentOnRemove by rememberUpdatedState(onRemove)
    val onDismiss = remember { { _: SwipeToDismissBoxValue -> currentOnRemove() } }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        onDismiss = onDismiss,
        backgroundContent = {
            val alignment = when (state.dismissDirection) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterEnd
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment,
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null, // decorative: the heart button is the accessible way to remove
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        // Opaque surface so the red background shows only where the row has moved away.
        Box(Modifier.background(MaterialTheme.colorScheme.surface)) { content() }
    }
}
