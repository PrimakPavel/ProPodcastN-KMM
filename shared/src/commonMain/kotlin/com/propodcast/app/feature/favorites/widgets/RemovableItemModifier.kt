package com.propodcast.app.feature.favorites.widgets

import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.ui.Modifier

/**
 * Animates moves and appearance, but not removal: a removed row that is still fading out would be reused
 * as-is (still swiped away) if Undo brings the same key back. A swiped row is already off-screen anyway.
 */
internal fun LazyGridItemScope.removableItemModifier(): Modifier = Modifier.animateItem(fadeOutSpec = null)
