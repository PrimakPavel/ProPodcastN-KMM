package com.propodcast.app.core.commonwidgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * How far a [FloatingHeader] is hidden: [heightOffset] goes from 0 (fully shown)
 * down to [heightOffsetLimit] (= minus the header height, fully hidden).
 * Survives configuration changes.
 */
@Stable
class FloatingHeaderState(initialHeightOffset: Float = 0f) {

    /** Set by [FloatingHeader] after measuring; always <= 0. */
    var heightOffsetLimit by mutableFloatStateOf(0f)
        internal set

    var heightOffset: Float
        get() = _heightOffset
        set(value) {
            _heightOffset = value.coerceIn(heightOffsetLimit, 0f)
        }

    private var _heightOffset by mutableFloatStateOf(initialHeightOffset)

    /** 0 = fully shown, 1 = fully hidden. */
    val collapsedFraction: Float
        get() = if (heightOffsetLimit != 0f) heightOffset / heightOffsetLimit else 0f

    internal fun updateLimit(limit: Float) {
        heightOffsetLimit = limit
        heightOffset = heightOffset // re-clamp when the header got shorter
    }

    companion object {
        val Saver: Saver<FloatingHeaderState, Float> = Saver(
            save = { it.heightOffset },
            restore = { FloatingHeaderState(it) },
        )
    }
}

@Composable
fun rememberFloatingHeaderState(): FloatingHeaderState =
    rememberSaveable(saver = FloatingHeaderState.Saver) { FloatingHeaderState() }
