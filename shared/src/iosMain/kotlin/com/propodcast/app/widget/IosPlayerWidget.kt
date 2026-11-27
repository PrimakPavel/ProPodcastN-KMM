package com.propodcast.app.widget

import co.touchlab.kermit.Logger
import com.propodcast.app.core.data.repository.UserPreferencesRepository
import com.propodcast.app.core.player.PlayerController
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.player_error_source
import com.propodcast.app.resources.player_pause
import com.propodcast.app.resources.player_play
import com.propodcast.app.resources.widget_empty
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Foundation.writeToFile

/** Reloads the WidgetKit timelines; implemented in Swift (WidgetCenter has no Objective-C API). */
interface WidgetReloader {
    fun reloadPlayerWidget()
}

/**
 * What the iOS widget extension reads from the App Group (it cannot run the shared code): the [PlayerWidgetState],
 * the artwork as a file in the group container, and the texts in the user's language.
 */
@Serializable
internal data class IosWidgetSnapshot(
    val episodeTitle: String? = null,
    val podcastTitle: String? = null,
    val isPlaying: Boolean = false,
    val hasError: Boolean = false,
    /** File name in the group container's `widget` folder, or null when there is no artwork. */
    val artworkFile: String? = null,
    val emptyText: String,
    val errorText: String,
    val playLabel: String,
    val pauseLabel: String,
)

/**
 * Publishes the player state to the iOS widget whenever what it shows changes (episode, play / pause, error),
 * as PlayerWidgetUpdater does for Glance. Started once Koin is up (see initKoinIos).
 */
@OptIn(ExperimentalForeignApi::class)
internal class IosPlayerWidgetPublisher(
    private val controller: PlayerController,
    private val preferences: UserPreferencesRepository,
    private val reloader: WidgetReloader,
) {
    private val log = Logger.withTag("IosPlayerWidget")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val http = HttpClient()
    private val json = Json { encodeDefaults = true }

    fun start() {
        val defaults = NSUserDefaults(suiteName = APP_GROUP)
        val container = NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(APP_GROUP)?.path
        if (container == null) {
            log.w { "No App Group container ($APP_GROUP): the widget gets no data" }
            return
        }
        val artworkDir = "$container/widget".also {
            NSFileManager.defaultManager.createDirectoryAtPath(it, withIntermediateDirectories = true, attributes = null, error = null)
        }
        scope.launch {
            combine(controller.state, preferences.lastPlayed, ::playerWidgetState)
                .distinctUntilChanged()
                .collect { state ->
                    val artwork = state?.imageUrl?.let { url -> saveArtwork(url, artworkDir) }
                    val snapshot = IosWidgetSnapshot(
                        episodeTitle = state?.episodeTitle,
                        podcastTitle = state?.podcastTitle,
                        isPlaying = state?.isPlaying == true,
                        hasError = state?.hasError == true,
                        artworkFile = artwork,
                        emptyText = getString(Res.string.widget_empty),
                        errorText = getString(Res.string.player_error_source),
                        playLabel = getString(Res.string.player_play),
                        pauseLabel = getString(Res.string.player_pause),
                    )
                    defaults.setObject(json.encodeToString(IosWidgetSnapshot.serializer(), snapshot), forKey = SNAPSHOT_KEY)
                    reloader.reloadPlayerWidget()
                }
        }
    }

    /** Downloads the artwork once per URL into the group container; the file name comes from the URL's hash. */
    private suspend fun saveArtwork(url: String, dir: String): String? {
        val name = "artwork-${url.hashCode().toUInt()}.img"
        val path = "$dir/$name"
        if (NSFileManager.defaultManager.fileExistsAtPath(path)) return name
        return try {
            val response = http.get(url)
            if (!response.status.isSuccess()) return null
            val bytes = response.readRawBytes()
            val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
            // Only the current artwork is kept.
            NSFileManager.defaultManager.contentsOfDirectoryAtPath(dir, error = null)?.forEach { old ->
                NSFileManager.defaultManager.removeItemAtPath("$dir/$old", error = null)
            }
            if (data.writeToFile(path, atomically = true)) name else null
        } catch (e: Exception) {
            log.w(e) { "Could not load the widget artwork" }
            null
        }
    }

    companion object {
        const val APP_GROUP = "group.com.propodcast.app.kmm"
        const val SNAPSHOT_KEY = "playerWidget"
    }
}

/** Called from the widget's play / pause AppIntent, which iOS runs in the app's process. */
object IosWidgetActions : KoinComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun togglePlayback() {
        scope.launch { toggleWidgetPlayback(controller = get(), preferences = get()) }
    }
}
