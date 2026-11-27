package com.propodcast.app.feature.downloads

import com.propodcast.app.resources.Res
import com.propodcast.app.resources.action_undo
import com.propodcast.app.resources.download_cancelled
import com.propodcast.app.resources.download_deleted
import com.propodcast.app.resources.download_retry
import com.propodcast.app.resources.download_waiting_wifi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.propodcast.app.core.connectivity.ConnectionType
import com.propodcast.app.core.connectivity.ConnectivityMonitor
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.Episode
import com.propodcast.app.core.model.EpisodeDownloadState
import com.propodcast.app.core.ui.SnackbarEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Download actions for any screen with episodes: asks before using mobile data; cancel and delete come
 * with "Retry" / "Undo" snackbars ([snackbarEvents], shown by SnackbarEffect).
 */
class DownloadsViewModel(
    private val repository: DownloadsRepository,
    private val connectivity: ConnectivityMonitor,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    val states: StateFlow<Map<String, EpisodeDownloadState>> = repository.states
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    private val mutableConfirmation = MutableStateFlow<Episode?>(null)

    /** An episode waiting for "Download over mobile data?"; null when no dialog is shown. */
    val confirmation: StateFlow<Episode?> = mutableConfirmation.asStateFlow()

    private val snackbarChannel = Channel<SnackbarEvent>(Channel.BUFFERED)
    val snackbarEvents: Flow<SnackbarEvent> = snackbarChannel.receiveAsFlow()

    /**
     * Wi-Fi / Ethernet: starts right away. Mobile data: asks first, or with "Download only on Wi-Fi" queues it
     * for Wi-Fi without asking. No network: queues it, WorkManager starts it once a network is back.
     */
    fun download(episode: Episode) {
        if (episode.audioUrl == null) return
        viewModelScope.launch {
            if (connectivity.currentConnection() != ConnectionType.METERED) {
                repository.enqueue(episode)
            } else if (preferences.downloadOnlyOnWifi.first()) {
                repository.enqueue(episode)
                snackbarChannel.send(SnackbarEvent(messageRes = Res.string.download_waiting_wifi))
            } else {
                mutableConfirmation.value = episode
            }
        }
    }

    fun confirmDownload() {
        mutableConfirmation.value?.let(::enqueue)
        mutableConfirmation.value = null
    }

    fun dismissConfirmation() {
        mutableConfirmation.value = null
    }

    fun cancel(episode: Episode) {
        viewModelScope.launch {
            repository.cancel(episode.id)
            snackbarChannel.send(
                SnackbarEvent(
                    messageRes = Res.string.download_cancelled,
                    actionLabelRes = Res.string.download_retry,
                    onAction = { download(episode) },
                ),
            )
        }
    }

    fun delete(episode: Episode) {
        viewModelScope.launch {
            // Hidden now, the file goes when the snackbar closes without Undo.
            repository.markRemoved(episode.id)
            snackbarChannel.send(
                SnackbarEvent(
                    messageRes = Res.string.download_deleted,
                    actionLabelRes = Res.string.action_undo,
                    onAction = { undoDelete(episode) },
                    onDismiss = { commitDelete(episode) },
                ),
            )
        }
    }

    fun undoDelete(episode: Episode) {
        viewModelScope.launch { repository.undoRemove(episode.id) }
    }

    fun commitDelete(episode: Episode) {
        viewModelScope.launch { repository.commitRemove(episode.id) }
    }

    private fun enqueue(episode: Episode) {
        viewModelScope.launch { repository.enqueue(episode) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
