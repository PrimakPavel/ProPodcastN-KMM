package com.propodcast.app.feature.player

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.feature.player.widgets.rememberDragToDismiss

/**
 * Full-height player sheet over the whole app (navigation bar included), on a dimming scrim. It slides up from the mini player while
 * the artwork flies between the two (shared element in [sharedTransitionScope]). Closes with back,
 * the ⌄ button or a pull down.
 */
@OptIn(ExperimentalMaterial3Api::class) // BottomSheetDefaults.ExpandedShape / ScrimColor
@Composable
fun FullPlayerOverlay(
    visible: Boolean,
    state: PlayerUiState,
    sharedTransitionScope: SharedTransitionScope,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    downloadState: EpisodeDownloadState? = null,
    onDownloadClick: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    @OptIn(ExperimentalComposeUiApi::class)
    BackHandler(enabled = visible, onBack = onCollapse)
    Box(modifier) {
        // Dims the app behind the player, like a sheet's scrim; tapping it (e.g. while pulling down) collapses.
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(BottomSheetDefaults.ScrimColor)
                    .pointerInput(onCollapse) { detectTapGestures { onCollapse() } },
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it / 4 } + fadeIn(),
            exit = slideOutVertically { it / 4 } + fadeOut(),
        ) {
            val artworkModifier = with(sharedTransitionScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(PLAYER_ARTWORK_KEY),
                    animatedVisibilityScope = this@AnimatedVisibility,
                )
            }
            // A full-height sheet: starts below the status bar (dimmed by the scrim) with rounded top corners.
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .then(rememberDragToDismiss(onCollapse)),
                shape = BottomSheetDefaults.ExpandedShape,
                color = BottomSheetDefaults.ContainerColor,
            ) {
                Box {
                    FullPlayerContent(
                        state = state,
                        onCollapse = onCollapse,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeekTo = onSeekTo,
                        onSeekBack = onSeekBack,
                        onSeekForward = onSeekForward,
                        onSpeedSelected = onSpeedSelected,
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                        ),
                        artworkModifier = artworkModifier,
                        downloadState = downloadState,
                        onDownloadClick = onDownloadClick,
                    )
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .windowInsetsPadding(WindowInsets.safeDrawing),
                    )
                }
            }
        }
    }
}
