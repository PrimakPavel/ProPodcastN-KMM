package com.propodcast.app.core.commonwidgets

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.Test

class FloatingHeaderScrollBehaviorTest {

    private val state = FloatingHeaderState().apply { updateLimit(-100f) }

    private fun behavior(pinned: Boolean = false) =
        FloatingHeaderScrollBehavior(state, pinned, snapAnimationSpec = null)

    private fun FloatingHeaderScrollBehavior.scroll(dy: Float): Float =
        nestedScrollConnection.onPreScroll(Offset(0f, dy), NestedScrollSource.UserInput).y

    @Test
    fun scrollDown_hidesHeaderAndConsumesOnlyWhatItUsed() {
        val behavior = behavior()

        assertEquals(-30f, behavior.scroll(-30f))
        assertEquals(-30f, state.heightOffset)

        // Only 70 px left to hide; the rest goes to the list.
        assertEquals(-70f, behavior.scroll(-100f))
        assertEquals(-100f, state.heightOffset)
        assertEquals(1f, state.collapsedFraction)

        // Already hidden: nothing consumed.
        assertEquals(0f, behavior.scroll(-50f))
    }

    @Test
    fun scrollUp_fromAnywhere_showsHeaderAgain() {
        val behavior = behavior()
        behavior.scroll(-100f)

        assertEquals(40f, behavior.scroll(40f))
        assertEquals(-60f, state.heightOffset)
        assertEquals(60f, behavior.scroll(500f))
        assertEquals(0f, state.heightOffset)
    }

    @Test
    fun pinned_neverMoves() {
        val behavior = behavior(pinned = true)

        assertEquals(0f, behavior.scroll(-50f))
        assertEquals(0f, state.heightOffset)
    }

    @Test
    fun fling_snapsHalfHiddenHeaderToNearestEnd() = runTest {
        val behavior = behavior()

        behavior.scroll(-60f)
        behavior.nestedScrollConnection.onPostFling(Velocity.Zero, Velocity.Zero)
        assertEquals(-100f, state.heightOffset)

        behavior.scroll(30f) // 70% hidden: snaps back to hidden
        behavior.nestedScrollConnection.onPostFling(Velocity.Zero, Velocity.Zero)
        assertEquals(-100f, state.heightOffset)

        behavior.scroll(70f) // 30% hidden: snaps to shown
        behavior.nestedScrollConnection.onPostFling(Velocity.Zero, Velocity.Zero)
        assertEquals(0f, state.heightOffset)
    }

    @Test
    fun shorterHeader_reclampsOffset() {
        behavior().scroll(-100f)

        state.updateLimit(-40f)

        assertEquals(-40f, state.heightOffset)
    }
}
