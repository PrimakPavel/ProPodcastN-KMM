package com.propodcast.app.core.player

import co.touchlab.kermit.Logger
import com.propodcast.app.core.analytics.AnalyticsEvents
import com.propodcast.app.core.analytics.AnalyticsTracker
import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.model.DownloadStatus
import com.propodcast.app.core.model.NowPlaying
import com.propodcast.app.core.model.toLastPlayed
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionInterruptionNotification
import platform.AVFAudio.AVAudioSessionInterruptionTypeBegan
import platform.AVFAudio.AVAudioSessionInterruptionTypeKey
import platform.AVFAudio.AVAudioSessionModeSpokenAudio
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.AVPlayerItemStatusFailed
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.defaultRate
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.rate
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.AVFoundation.setDefaultRate
import platform.AVFoundation.setRate
import platform.AVFoundation.timeControlStatus
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.NSURLErrorDomain
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.UIKit.UIImage
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatus
import platform.MediaPlayer.MPRemoteCommandHandlerStatusCommandFailed
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.darwin.dispatch_get_main_queue

/**
 * [PlayerController] on AVPlayer. Background audio comes from the `audio` background mode in Info.plist and the
 * playback audio session; lock screen, Control Center and headphone controls from MPRemoteCommandCenter and
 * MPNowPlayingInfoCenter. Saves the last episode and position like the Android player does.
 * Call from the main thread (all callbacks arrive on the main queue).
 */
@OptIn(ExperimentalForeignApi::class)
class IosPlayerController(
    private val preferences: UserPreferencesRepository,
    private val downloads: DownloadsRepository,
    private val analytics: AnalyticsTracker,
) : PlayerController {

    private val log = Logger.withTag("IosPlayerController")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val player = AVPlayer()

    private var current: NowPlaying? = null
    private var playsLocalFile = false

    /** What the user asked for ("play"), as Media3's playWhenReady: true also while buffering. */
    private var wantsToPlay = false
    private var ended = false
    private var speed = 1f
    private var ticks = 0
    private var lastNowPlayingKey: Any? = null

    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    init {
        player.addPeriodicTimeObserverForInterval(
            CMTimeMakeWithSeconds(PROGRESS_INTERVAL_SECONDS, TIME_SCALE),
            dispatch_get_main_queue(),
        ) { _ ->
            noticeExternalPause()
            publish()
            if (wantsToPlay && !ended && ++ticks % SAVE_EVERY_TICKS == 0) savePosition()
        }
        val center = NSNotificationCenter.defaultCenter
        center.addObserverForName(AVPlayerItemDidPlayToEndTimeNotification, null, NSOperationQueue.mainQueue) { note ->
            if (note?.`object` != player.currentItem) return@addObserverForName
            ended = true
            wantsToPlay = false
            publish()
            // Finished: next time "continue" starts from the beginning.
            scope.launch { preferences.updateLastPlayedPosition(0) }
        }
        center.addObserverForName(AVAudioSessionInterruptionNotification, null, NSOperationQueue.mainQueue) { note ->
            val type = (note?.userInfo?.get(AVAudioSessionInterruptionTypeKey) as? NSNumber)?.unsignedLongValue
            if (type == AVAudioSessionInterruptionTypeBegan) {
                // A call or another app took the audio: the system paused us.
                wantsToPlay = false
                publish()
                savePosition()
            }
        }
        setUpRemoteCommands()
        followDownloads()
    }

    override fun play(item: NowPlaying, startPositionMs: Long) {
        scope.launch {
            // A downloaded episode plays from its file: works offline and saves data.
            val localPath = downloads.localFilePath(item.episodeId)
            val url = localPath?.let { NSURL.fileURLWithPath(it) } ?: NSURL.URLWithString(item.audioUrl)
            if (url == null) {
                log.w { "No playable URL for ${item.episodeId}" }
                return@launch
            }
            log.d { "Playing ${item.episodeId} from ${if (localPath != null) "downloaded file" else "stream"}" }
            analytics.logEvent(
                AnalyticsEvents.PLAY_EPISODE,
                mapOf(
                    AnalyticsEvents.PARAM_SOURCE to
                        if (localPath != null) AnalyticsEvents.SOURCE_DOWNLOAD else AnalyticsEvents.SOURCE_STREAM,
                ),
            )
            load(item, url, startPositionMs, isLocalFile = localPath != null)
            resume()
            preferences.setLastPlayed(item.toLastPlayed(startPositionMs))
        }
    }

    override fun togglePlayPause() {
        when {
            current == null -> Unit
            wantsToPlay && !ended && failed() == null -> pause()
            ended -> {
                seekTo(0)
                resume()
            }
            failed() != null -> scope.launch {
                // After an error (e.g. the stream lost the network) the item cannot continue: load it again,
                // from the downloaded file if there is one by now, otherwise from the same source.
                if (!switchToDownloadedFile()) current?.let { reload(it) }
                resume()
            }
            else -> resume()
        }
    }

    override fun seekTo(positionMs: Long) {
        val target = positionMs.coerceAtLeast(0)
        player.seekToTime(CMTimeMakeWithSeconds(target / 1_000.0, TIME_SCALE))
        if (ended && target < durationMs()) ended = false
        publish(positionOverrideMs = target)
        updateNowPlayingInfo(force = true, positionMs = target)
    }

    override fun seekBack() = seekTo(positionMs() - PlayerController.SEEK_BACK_MS)

    override fun seekForward() = seekTo((positionMs() + PlayerController.SEEK_FORWARD_MS).coerceAtMost(durationMs()))

    override fun setSpeed(speed: Float) {
        this.speed = speed
        player.setDefaultRate(speed)
        if (wantsToPlay && !ended) player.setRate(speed)
        publish()
    }

    override fun stop() {
        player.pause()
        player.replaceCurrentItemWithPlayerItem(null)
        current = null
        wantsToPlay = false
        ended = false
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
        lastNowPlayingKey = null
        publish()
        scope.launch { preferences.clearLastPlayed() }
    }

    private fun load(item: NowPlaying, url: NSURL, startPositionMs: Long, isLocalFile: Boolean) {
        activateAudioSession()
        player.replaceCurrentItemWithPlayerItem(AVPlayerItem(uRL = url))
        if (startPositionMs > 0) player.seekToTime(CMTimeMakeWithSeconds(startPositionMs / 1_000.0, TIME_SCALE))
        current = item
        playsLocalFile = isLocalFile
        ended = false
        publish(positionOverrideMs = startPositionMs)
    }

    private fun reload(item: NowPlaying) {
        val url = NSURL.URLWithString(item.audioUrl) ?: return
        load(item, url, positionMs(), isLocalFile = false)
    }

    private fun resume() {
        if (current == null) return
        wantsToPlay = true
        player.setDefaultRate(speed)
        player.play()
        publish()
        updateNowPlayingInfo(force = true)
    }

    private fun pause() {
        wantsToPlay = false
        player.pause()
        publish()
        updateNowPlayingInfo(force = true)
        savePosition()
    }

    /**
     * Moves the loaded episode from its stream to the downloaded file at the same position, keeping
     * play / pause. False when it already plays a file or has no finished download.
     */
    private suspend fun switchToDownloadedFile(): Boolean {
        val item = current ?: return false
        if (playsLocalFile) return false
        val path = downloads.localFilePath(item.episodeId) ?: return false
        val position = positionMs()
        val playing = wantsToPlay
        load(item, NSURL.fileURLWithPath(path), position, isLocalFile = true)
        if (playing) resume()
        log.d { "Switched ${item.episodeId} from stream to downloaded file at $position ms" }
        return true
    }

    /** Once the episode in the player finishes downloading, it continues from the file (works offline). */
    private fun followDownloads() {
        scope.launch {
            downloads.states
                .map { states -> current?.episodeId?.let { states[it]?.status } }
                .distinctUntilChanged()
                .filter { it == DownloadStatus.DOWNLOADED }
                .collect { switchToDownloadedFile() }
        }
    }

    /** AVPlayer pauses on its own when headphones are unplugged; the play button should follow. */
    private fun noticeExternalPause() {
        if (wantsToPlay && !ended && failed() == null &&
            player.timeControlStatus == AVPlayerTimeControlStatusPaused && player.rate == 0f
        ) {
            wantsToPlay = false
            savePosition()
        }
    }

    private fun publish(positionOverrideMs: Long? = null) {
        val error = failed()
        mutableState.value = PlayerState(
            nowPlaying = current,
            isPlaying = wantsToPlay && !ended && error == null,
            isBuffering = wantsToPlay && player.timeControlStatus == AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate,
            positionMs = positionOverrideMs ?: positionMs(),
            durationMs = durationMs(),
            speed = speed,
            error = error,
        )
        updateNowPlayingInfo(force = false)
    }

    private fun failed(): PlayerError? {
        val item = player.currentItem ?: return null
        if (item.status != AVPlayerItemStatusFailed) return null
        return if (item.error?.domain == NSURLErrorDomain) PlayerError.NETWORK else PlayerError.SOURCE
    }

    private fun positionMs(): Long {
        val seconds = CMTimeGetSeconds(player.currentTime())
        return if (seconds.isNaN() || seconds < 0) 0 else (seconds * 1_000).toLong()
    }

    private fun durationMs(): Long {
        val seconds = player.currentItem?.let { CMTimeGetSeconds(it.duration) }
        return if (seconds != null && !seconds.isNaN() && !seconds.isInfinite() && seconds > 0) {
            (seconds * 1_000).toLong()
        } else {
            current?.durationMs ?: 0
        }
    }

    private fun savePosition() {
        if (current == null) return
        val position = positionMs()
        scope.launch { preferences.updateLastPlayedPosition(position) }
    }

    private fun activateAudioSession() {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, mode = AVAudioSessionModeSpokenAudio, options = 0u, error = null)
        session.setActive(true, error = null)
    }

    /** Lock screen / Control Center info. Rewritten when the episode, play state, duration or speed changes. */
    private fun updateNowPlayingInfo(force: Boolean, positionMs: Long = positionMs()) {
        val item = current ?: return
        val playing = wantsToPlay && !ended
        val key = listOf(item.episodeId, playing, durationMs(), speed)
        if (!force && key == lastNowPlayingKey) return
        lastNowPlayingKey = key
        MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = buildMap<Any?, Any?> {
            put(MPMediaItemPropertyTitle, item.title)
            put(MPMediaItemPropertyArtist, item.podcastTitle)
            put(MPMediaItemPropertyPlaybackDuration, durationMs() / 1_000.0)
            put(MPNowPlayingInfoPropertyElapsedPlaybackTime, positionMs / 1_000.0)
            put(MPNowPlayingInfoPropertyPlaybackRate, if (playing) speed.toDouble() else 0.0)
            artwork?.takeIf { it.first == item.imageUrl }?.let { put(MPMediaItemPropertyArtwork, it.second) }
        }
        loadArtwork(item)
    }

    /** The episode's artwork for the lock screen, as (image url, artwork); loaded once per url. */
    private var artwork: Pair<String, MPMediaItemArtwork>? = null
    private var artworkLoading: String? = null
    private val artworkClient by lazy { HttpClient() }

    private fun loadArtwork(item: NowPlaying) {
        val url = item.imageUrl ?: return
        if (artwork?.first == url || artworkLoading == url) return
        artworkLoading = url
        scope.launch {
            val image = try {
                val bytes = artworkClient.get(url).takeIf { it.status.isSuccess() }?.readRawBytes()
                bytes?.let { data -> data.usePinned { UIImage(data = NSData.create(bytes = it.addressOf(0), length = data.size.toULong())) } }
            } catch (e: Exception) {
                log.w(e) { "Could not load the lock screen artwork" }
                null
            }
            if (artworkLoading == url) artworkLoading = null
            if (image == null || current?.imageUrl != url) return@launch
            artwork = url to MPMediaItemArtwork(boundsSize = image.size) { _ -> image }
            updateNowPlayingInfo(force = true)
        }
    }

    /** Podcasts: -15 s / +30 s around play/pause instead of previous / next track, plus scrubbing. */
    private fun setUpRemoteCommands() {
        val commands = MPRemoteCommandCenter.sharedCommandCenter()
        fun handled(action: () -> Unit): MPRemoteCommandHandlerStatus =
            if (current == null) {
                MPRemoteCommandHandlerStatusCommandFailed
            } else {
                action()
                MPRemoteCommandHandlerStatusSuccess
            }
        commands.playCommand.addTargetWithHandler { handled { if (!wantsToPlay) togglePlayPause() } }
        commands.pauseCommand.addTargetWithHandler { handled { if (wantsToPlay) pause() } }
        commands.togglePlayPauseCommand.addTargetWithHandler { handled { togglePlayPause() } }
        commands.skipBackwardCommand.preferredIntervals = listOf(PlayerController.SEEK_BACK_MS / 1_000)
        commands.skipBackwardCommand.addTargetWithHandler { handled { seekBack() } }
        commands.skipForwardCommand.preferredIntervals = listOf(PlayerController.SEEK_FORWARD_MS / 1_000)
        commands.skipForwardCommand.addTargetWithHandler { handled { seekForward() } }
        commands.changePlaybackPositionCommand.addTargetWithHandler { event ->
            val seconds = (event as? MPChangePlaybackPositionCommandEvent)?.positionTime ?: return@addTargetWithHandler MPRemoteCommandHandlerStatusCommandFailed
            handled { seekTo((seconds * 1_000).toLong()) }
        }
        commands.nextTrackCommand.enabled = false
        commands.previousTrackCommand.enabled = false
    }

    private companion object {
        const val PROGRESS_INTERVAL_SECONDS = 0.5
        const val TIME_SCALE = 1_000
        const val SAVE_EVERY_TICKS = 10 // every 5 s
    }
}
