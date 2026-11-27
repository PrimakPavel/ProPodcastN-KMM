package com.propodcast.app.core.commonwidgets

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Scroll behavior for a [FloatingHeader], like Flutter's `SliverAppBar(floating: true)` /
 * Material's "enter always": the header hides while the content scrolls down and comes back
 * as soon as it scrolls up, from any position in the list.
 *
 * Attach [nestedScrollConnection] with `Modifier.nestedScroll` to a parent of the scrolling list.
 * With [pinned] = true the header never moves (handy to switch the behavior off in code).
 * After a drag or fling ends half-way, the header snaps to fully shown or hidden.
 */
@Stable
class FloatingHeaderScrollBehavior(
    val state: FloatingHeaderState,
    val pinned: Boolean = false,
    private val snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = SNAP_STIFFNESS),
) {
    val nestedScrollConnection: NestedScrollConnection = object : NestedScrollConnection {

        // Runs before the list scrolls: move the header first and consume that part of the scroll,
        // so the list does not move while the header is sliding in or out.
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (pinned || available.y == 0f) return Offset.Zero
            val previous = state.heightOffset
            state.heightOffset = previous + available.y
            val consumed = state.heightOffset - previous
            return Offset(0f, consumed)
        }

        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            if (!pinned) settle()
            return Velocity.Zero
        }
    }

    /** Snaps a half-hidden header to the nearest end (shown or hidden). */
    suspend fun settle() {
        val offset = state.heightOffset
        val limit = state.heightOffsetLimit
        if (offset == 0f || offset == limit) return
        val target = if (offset < limit / 2) limit else 0f
        val spec = snapAnimationSpec
        if (spec == null) {
            state.heightOffset = target
        } else {
            AnimationState(initialValue = offset).animateTo(target, spec) { state.heightOffset = value }
        }
    }

    private companion object {
        const val SNAP_STIFFNESS = 1_500f
    }
}

@Composable
fun rememberFloatingHeaderScrollBehavior(
    pinned: Boolean = false,
    state: FloatingHeaderState = rememberFloatingHeaderState(),
): FloatingHeaderScrollBehavior = remember(state, pinned) { FloatingHeaderScrollBehavior(state, pinned) }
