package com.propodcast.app.core.download

import android.content.Context
import java.io.File

/**
 * Where downloaded episodes live: app-private storage (no permission needed, removed with the app).
 * A download is written to a `.part` file and renamed when complete, so a cut-off download never
 * looks finished.
 */
class EpisodeFileStore(
    private val context: Context,
) {
    private val dir: File
        get() = File(context.filesDir, DIR).apply { mkdirs() }

    fun file(episodeId: String): File = File(dir, "${safeName(episodeId)}.mp3")

    fun partFile(episodeId: String): File = File(dir, "${safeName(episodeId)}.mp3.part")

    /** Turns the finished `.part` into the episode file. */
    fun commit(episodeId: String): File {
        val target = file(episodeId)
        check(partFile(episodeId).renameTo(target)) { "Could not move the download into place" }
        return target
    }

    fun deletePart(episodeId: String) {
        partFile(episodeId).delete()
    }

    fun delete(episodeId: String) {
        file(episodeId).delete()
        deletePart(episodeId)
    }

    /** Episode ids are API ids, but keep file names safe anyway. */
    private fun safeName(episodeId: String) = episodeId.replace(Regex("[^A-Za-z0-9_-]"), "_")

    private companion object {
        const val DIR = "episodes"
    }
}
