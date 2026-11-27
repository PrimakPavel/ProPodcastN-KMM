package com.propodcast.app.core.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.core.net.toUri
import com.propodcast.app.core.analytics.AnalyticsEvents
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.model.toLastPlayed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import co.touchlab.kermit.Logger
import java.io.File

/**
 * [PlayerController] backed by a Media3 [MediaController] connected to [PlaybackService].
 * Lives as long as the process, so it also saves the last episode and position while the app is in background.
 */
class MediaPlayerController(
    private val context: Context,
    private val preferences: UserPreferencesRepository,
    private val downloads: DownloadsRepository,
    private val analytics: AnalyticsTracker,
) : PlayerController {

    // MediaController must be used on the main thread.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectMutex = Mutex()
    private var controller: MediaController? = null
    private var progressJob: Job? = null

    private val log = Logger.withTag("MediaPlayerController")
    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publish(player)
            if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) onIsPlayingChanged(player)
            if (events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED) && player.playbackState == Player.STATE_ENDED) {
                // Finished: next time "continue" starts from the beginning.
                scope.launch { preferences.updateLastPlayedPosition(0) }
            }
        }
    }

    override fun play(item: NowPlaying, startPositionMs: Long) {
        withController {
            // A downloaded episode plays from its file: works offline and saves data.
            val localFile = downloads.localFilePath(item.episodeId)?.let(::File)
            val playUri = localFile?.toUri() ?: item.audioUrl.toUri()
            log.d { "Playing ${item.episodeId} from ${if (localFile != null) "downloaded file" else "stream"}" }
            analytics.logEvent(
                AnalyticsEvents.PLAY_EPISODE,
                mapOf(
                    AnalyticsEvents.PARAM_SOURCE to
                        if (localFile != null) AnalyticsEvents.SOURCE_DOWNLOAD else AnalyticsEvents.SOURCE_STREAM,
                ),
            )
            setMediaItem(item.toMediaItem(playUri), startPositionMs)
            prepare()
            play()
            preferences.setLastPlayed(item.toLastPlayed(startPositionMs))
        }
    }

    override fun togglePlayPause() = withController {
        when {
            playWhenReady && playbackState != Player.STATE_ENDED && playerError == null -> pause()
            playbackState == Player.STATE_ENDED -> {
                seekTo(0)
                play()
            }
            else -> {
                // After an error (e.g. the stream lost the network) the player is idle and must be prepared
                // again: from the downloaded file if there is one by now, otherwise the same source.
                if (playbackState == Player.STATE_IDLE && !switchToDownloadedFile()) prepare()
                play()
            }
        }
    }

    override fun seekTo(positionMs: Long) = withController { seekTo(positionMs.coerceAtLeast(0)) }

    override fun seekBack() = withController { seekBack() }

    override fun seekForward() = withController { seekForward() }

    override fun setSpeed(speed: Float) = withController { setPlaybackSpeed(speed) }

    override fun stop() {
        withController {
            stop()
            clearMediaItems()
            preferences.clearLastPlayed()
        }
    }

    /**
     * Moves the loaded episode from its stream to the downloaded file at the same position, keeping
     * play / pause. False when it already plays a file or has no finished download.
     */
    private suspend fun MediaController.switchToDownloadedFile(): Boolean {
        val current = currentMediaItem ?: return false
        if (current.playsLocalFile()) return false
        val item = current.toNowPlaying() ?: return false
        val file = downloads.localFilePath(item.episodeId)?.let(::File) ?: return false
        val position = currentPosition
        setMediaItem(item.toMediaItem(file.toUri()), position)
        prepare()
        log.d { "Switched ${item.episodeId} from stream to downloaded file at $position ms" }
        return true
    }

    /** Once the episode in the player finishes downloading, it continues from the file (works offline). */
    private fun followDownloads(controller: MediaController) {
        scope.launch {
            downloads.states
                .map { states -> controller.currentMediaItem?.mediaId?.let { states[it]?.status } }
                .distinctUntilChanged()
                .filter { it == DownloadStatus.DOWNLOADED }
                .collect {
                    try {
                        controller.switchToDownloadedFile()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        log.e(e) { "Could not switch to the downloaded file" }
                    }
                }
        }
    }

    private fun withController(action: suspend MediaController.() -> Unit) {
        scope.launch {
            try {
                connectedController().action()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.e(e) { "Player action failed" }
            }
        }
    }

    private suspend fun connectedController(): MediaController = connectMutex.withLock {
        controller ?: MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java)),
        ).buildAsync().await().also { connected ->
            connected.addListener(listener)
            controller = connected
            followDownloads(connected)
            publish(connected)
            onIsPlayingChanged(connected)
        }
    }

    private fun publish(player: Player) {
        val nowPlaying = player.currentMediaItem?.toNowPlaying()
        mutableState.value = PlayerState(
            nowPlaying = nowPlaying,
            isPlaying = player.playWhenReady && player.playbackState != Player.STATE_ENDED && player.playerError == null,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.takeIf { it != C.TIME_UNSET && it > 0 } ?: nowPlaying?.durationMs ?: 0,
            speed = player.playbackParameters.speed,
            error = player.playerError?.toPlayerError(),
        )
    }

    /** While playing: refresh the position for the UI and save it now and then. */
    private fun onIsPlayingChanged(player: Player) {
        progressJob?.cancel()
        if (player.isPlaying) {
            progressJob = scope.launch {
                var ticks = 0
                while (isActive) {
                    delay(PROGRESS_INTERVAL_MS)
                    publish(player)
                    if (++ticks % SAVE_EVERY_TICKS == 0) savePosition(player)
                }
            }
        } else if (player.currentMediaItem != null && player.playbackState != Player.STATE_ENDED) {
            scope.launch { savePosition(player) }
        }
    }

    private suspend fun savePosition(player: Player) {
        preferences.updateLastPlayedPosition(player.currentPosition.coerceAtLeast(0))
    }

    private fun PlaybackException.toPlayerError(): PlayerError = when (errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        -> PlayerError.NETWORK
        else -> PlayerError.SOURCE
    }

    private companion object {
        const val PROGRESS_INTERVAL_MS = 500L
        const val SAVE_EVERY_TICKS = 10 // every 5 s
    }
}
