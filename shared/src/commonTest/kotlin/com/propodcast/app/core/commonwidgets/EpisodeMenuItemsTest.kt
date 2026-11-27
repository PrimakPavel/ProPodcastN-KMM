package com.propodcast.app.core.commonwidgets

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_cancel_download
import com.propodcast.app.resources.action_delete_download
import com.propodcast.app.resources.action_download
import com.propodcast.app.resources.action_share
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.testing.testEpisode
import com.propodcast.app.core.ui.EpisodeDownloadActions
import kotlin.test.assertEquals
import kotlin.test.Test
import org.jetbrains.compose.resources.StringResource

class EpisodeMenuItemsTest {

    private val episode = testEpisode("e1")
    private val calls = mutableListOf<String>()
    private val actions = EpisodeDownloadActions(
        onDownload = { calls += "download" },
        onCancel = { calls += "cancel" },
        onDelete = { calls += "delete" },
    )

    private fun labels(state: EpisodeDownloadState?, share: Boolean = true) =
        episodeMenuItems(episode, state, actions, onShare = if (share) ({}) else null).map { it.labelRes }

    @Test
    fun menuFollowsTheDownloadState() {
        assertEquals(listOf(Res.string.action_share, Res.string.action_download), labels(null))
        assertEquals(
            listOf(Res.string.action_share, Res.string.action_cancel_download),
            labels(EpisodeDownloadState(DownloadStatus.DOWNLOADING, progress = 0.3f)),
        )
        assertEquals(
            listOf(Res.string.action_share, Res.string.action_delete_download),
            labels(EpisodeDownloadState(DownloadStatus.DOWNLOADED)),
        )
        assertEquals(
            listOf(Res.string.action_download, Res.string.action_delete_download),
            labels(EpisodeDownloadState(DownloadStatus.FAILED), share = false),
        )
    }

    @Test
    fun noAudio_noDownloadEntry() {
        val items = episodeMenuItems(episode.copy(audioUrl = null), null, actions, onShare = null)
        assertEquals(emptyList<StringResource>(), items.map { it.labelRes })
    }

    @Test
    fun itemsRunTheirAction() {
        episodeMenuItems(episode, EpisodeDownloadState(DownloadStatus.QUEUED), actions, onShare = null).single().onClick()
        assertEquals(listOf("cancel"), calls)
    }
}
