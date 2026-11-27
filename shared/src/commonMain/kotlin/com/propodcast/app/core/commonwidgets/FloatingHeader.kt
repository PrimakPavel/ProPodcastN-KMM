package com.propodcast.app.core.commonwidgets

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/**
 * Header that takes less vertical space as [scrollBehavior] hides it, so the content below
 * moves up with it. Put it above the scrolling list in a Column, e.g.:
 *
 * ```
 * val behavior = rememberFloatingHeaderScrollBehavior()
 * Column(Modifier.nestedScroll(behavior.nestedScrollConnection)) {
 *     FloatingHeader(behavior) { FilterChips() }
 *     LazyColumn { ... }
 * }
 * ```
 */
@Composable
fun FloatingHeader(
    scrollBehavior: FloatingHeaderScrollBehavior,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = scrollBehavior.state
    Box(
        modifier = modifier
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minHeight = 0))
                val limit = -placeable.height.toFloat()
                if (state.heightOffsetLimit != limit) state.updateLimit(limit)
                val offset = state.heightOffset.roundToInt()
                val height = (placeable.height + offset).coerceAtLeast(0)
                layout(placeable.width, height) { placeable.place(0, offset) }
            },
    ) {
        content()
    }
}
