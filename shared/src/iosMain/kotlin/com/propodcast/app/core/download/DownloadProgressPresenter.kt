package com.propodcast.app.core.download

import com.propodcast.app.core.data.repository.DownloadsRepository
import com.propodcast.app.resources.Res
import com.propodcast.app.resources.download_cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Shows running downloads outside the app; implemented in Swift as Live Activities (ActivityKit is Swift-only).
 * iOS has no ongoing progress notifications: this is the equivalent of the Android download notification.
 */
interface DownloadProgressPresenter {
    /** Starts or updates the episode's activity. [progress] is 0..1, or null while the size is unknown. */
    fun show(episodeId: String, title: String, cancelLabel: String, progress: Float?)

    fun hide(episodeId: String)
}

/**
 * Mirrors [progress] (running downloads by episode) to the [presenter]. A new download shows at once (iOS starts
 * a Live Activity only while the app is in the foreground, i.e. right when the user taps Download); updates come
 * at most once a second: Live Activities have an update budget, and a progress bar does not need more.
 */
internal fun CoroutineScope.presentDownloadProgress(
    progress: StateFlow<Map<String, Float?>>,
    presenter: DownloadProgressPresenter,
    titleOf: suspend (String) -> String?,
) = launch {
    val cancelLabel = getString(Res.string.download_cancel)
    val shown = mutableMapOf<String, Float?>()
    // StateFlow collection is conflated, so the delay below throttles updates without queueing them.
    progress.collect { running ->
        (shown.keys - running.keys).forEach { id ->
            presenter.hide(id)
            shown.remove(id)
        }
        running.forEach { (id, value) ->
            if (id !in shown || shown[id] != value) {
                val title = titleOf(id) ?: return@forEach
                presenter.show(id, title, cancelLabel, value)
                shown[id] = value
            }
        }
        delay(UPDATE_INTERVAL_MS)
    }
}

private const val UPDATE_INTERVAL_MS = 1_000L

/** Called from the Live Activity's Cancel intent, which iOS runs in the app's process. */
object IosDownloadActions : KoinComponent {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun cancel(episodeId: String) {
        scope.launch { get<DownloadsRepository>().cancel(episodeId) }
    }
}
