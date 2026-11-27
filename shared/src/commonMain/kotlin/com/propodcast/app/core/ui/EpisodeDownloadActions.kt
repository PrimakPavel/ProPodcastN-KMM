package com.propodcast.app.core.ui

import androidx.compose.runtime.Immutable
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownloadState

/**
 * Download states of episodes plus what the user can do with them, passed down to episode lists in one
 * piece. The default has no downloads and no actions (previews, tests).
 */
@Immutable
data class EpisodeDownloadActions(
    val states: Map<String, EpisodeDownloadState> = emptyMap(),
    val onDownload: ((Episode) -> Unit)? = null,
    val onCancel: ((Episode) -> Unit)? = null,
    val onDelete: ((Episode) -> Unit)? = null,
) {
    fun stateOf(episode: Episode): EpisodeDownloadState? = states[episode.id]

    /** The one-button action for the current state: download / cancel / delete / retry. */
    fun onPrimaryAction(episode: Episode) {
        val state = stateOf(episode)
        when {
            state == null || state.status == DownloadStatus.FAILED -> onDownload?.invoke(episode)
            state.isActive -> onCancel?.invoke(episode)
            else -> onDelete?.invoke(episode)
        }
    }
}
