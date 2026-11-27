package com.propodcast.app.feature.player

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_collapse
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.propodcast.app.core.commonwidgets.PodcastImage
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.feature.player.widgets.PlayerControls
import com.propodcast.app.feature.player.widgets.PlayerTitles

/**
 * Stateless full player: artwork, titles, seek bar, -15 / play / +30, speed menu and download; in landscape
 * the artwork sits beside the controls. Also used by UI tests.
 */
@OptIn(ExperimentalMaterial3Api::class) // BottomSheetDefaults.DragHandle
@Composable
fun FullPlayerContent(
    state: PlayerUiState,
    onCollapse: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeedSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
    /** Extra modifier for the artwork, e.g. a shared element transition from the mini player. */
    artworkModifier: Modifier = Modifier,
    downloadState: EpisodeDownloadState? = null,
    /** Download / cancel / delete / retry for the current [downloadState]; null hides the button. */
    onDownloadClick: (() -> Unit)? = null,
) {
    val playback = state.playback
    val item = playback.nowPlaying ?: return
    val controls: @Composable (Modifier) -> Unit = { controlsModifier ->
        PlayerControls(
            playback = playback,
            onTogglePlayPause = onTogglePlayPause,
            onSeekTo = onSeekTo,
            onSeekBack = onSeekBack,
            onSeekForward = onSeekForward,
            onSpeedSelected = onSpeedSelected,
            downloadState = downloadState,
            onDownloadClick = onDownloadClick,
            modifier = controlsModifier,
        )
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .testTag(FULL_PLAYER_TAG),
    ) {
        val landscape = maxWidth > maxHeight * LANDSCAPE_RATIO
        Column(Modifier.fillMaxSize()) {
            val collapseButton = @Composable { buttonModifier: Modifier ->
                IconButton(onClick = onCollapse, modifier = buttonModifier) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(Res.string.player_collapse))
                }
            }
            if (landscape) {
                // Low screen: ⌄ beside the handle instead of under it, so the content starts higher.
                Box(Modifier.fillMaxWidth()) {
                    BottomSheetDefaults.DragHandle(Modifier.align(Alignment.Center))
                    collapseButton(Modifier.align(Alignment.CenterStart))
                }
            } else {
                BottomSheetDefaults.DragHandle(Modifier.align(Alignment.CenterHorizontally))
                collapseButton(Modifier)
            }
            if (landscape) {
                // Artwork on the left at full height, titles and controls on the right.
                Row(
                    modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PodcastImage(
                        url = item.imageUrl,
                        modifier = artworkModifier
                            .weight(0.4f, fill = false)
                            .aspectRatio(1f),
                    )
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PlayerTitles(state)
                        Spacer(Modifier.height(16.dp))
                        controls(Modifier.widthIn(max = 480.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PodcastImage(
                        url = item.imageUrl,
                        modifier = artworkModifier
                            .widthIn(max = 320.dp)
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f),
                    )
                    Spacer(Modifier.height(24.dp))
                    PlayerTitles(state)
                    Spacer(Modifier.height(16.dp))
                    controls(Modifier.fillMaxWidth())
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

/** Wider than this many times its height: artwork beside the controls instead of above them. */
private const val LANDSCAPE_RATIO = 1.2f
