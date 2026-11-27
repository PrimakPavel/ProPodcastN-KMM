package com.propodcast.app.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

/** DataStore file names; the platform part adds the folder (see `dataStorePath`). */
object DataStoreFiles {
    /** Filters, last played episode, settings. */
    const val USER_PREFERENCES = "user_preferences"

    /** Genres, regions, languages (ReferenceDataCache), kept apart from user preferences. */
    const val REFERENCE_CACHE = "reference_cache"

    /** DataStore requires this extension for Preferences files. */
    fun fileName(name: String) = "$name.preferences_pb"
}

/** One instance per file per process: DataStore fails when two instances open the same file. */
fun createPreferencesDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { path.toPath() },
    )
