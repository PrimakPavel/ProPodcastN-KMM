package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.ic_download
import com.propodcast.app.resources.action_cancel_download
import com.propodcast.app.resources.action_delete_download
import com.propodcast.app.resources.action_download
import com.propodcast.app.resources.action_share
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.ui.EpisodeDownloadActions

/** One entry of an episode row's ⋮ menu. */
internal class EpisodeMenuItem(
    val labelRes: StringResource,
    val icon: @Composable () -> Painter,
    val onClick: () -> Unit,
)

/** Share (when possible) and the download action for the current state. */
internal fun episodeMenuItems(
    episode: Episode,
    state: EpisodeDownloadState?,
    downloads: EpisodeDownloadActions,
    onShare: (() -> Unit)?,
): List<EpisodeMenuItem> = buildList {
    if (onShare != null) add(EpisodeMenuItem(Res.string.action_share, { rememberVectorPainter(Icons.Filled.Share) }, onShare))
    val download = downloads.onDownload?.let { action ->
        EpisodeMenuItem(Res.string.action_download, { painterResource(Res.drawable.ic_download) }) { action(episode) }
    }
    val delete = downloads.onDelete?.let { action ->
        EpisodeMenuItem(Res.string.action_delete_download, { rememberVectorPainter(Icons.Filled.Delete) }) { action(episode) }
    }
    when (state?.status) {
        null -> if (episode.audioUrl != null) addIfNotNull(download)
        DownloadStatus.QUEUED, DownloadStatus.DOWNLOADING -> downloads.onCancel?.let { action ->
            add(EpisodeMenuItem(Res.string.action_cancel_download, { rememberVectorPainter(Icons.Filled.Close) }) { action(episode) })
        }
        DownloadStatus.DOWNLOADED -> addIfNotNull(delete)
        DownloadStatus.FAILED -> {
            addIfNotNull(download)
            addIfNotNull(delete)
        }
    }
}

private fun <T> MutableList<T>.addIfNotNull(item: T?) {
    if (item != null) add(item)
}
